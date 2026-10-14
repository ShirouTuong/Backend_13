# [BÀI TẬP] LUYỆN TẬP MYSQL JDBC TRANSACTION (BTH6-BE13)

Dự án bài tập thực hành nâng cao về **JDBC Transaction** trong ứng dụng **User Management (Quản lý người dùng)** sử dụng **Java Servlet (Jakarta EE 10)**, **JDBC Connection (`commit()` & `rollback()`)**, và hệ quản trị cơ sở dữ liệu **MySQL 8.0**.

---

## 🎯 1. Mục Tiêu & Yêu Cầu Đề Bài

1. **Hiểu bản chất và vận dụng thành thạo JDBC Transaction:**
   - Sử dụng các phương thức `connection.setAutoCommit(false)`, `connection.commit()`, và `connection.rollback()` của đối tượng `Connection`.
   - Đảm bảo tuân thủ nguyên lý **Atomicity (All-or-Nothing)** trong bộ tiêu chuẩn **ACID**: Hoặc là tất cả các câu lệnh trong giao dịch cùng thành công và được lưu vĩnh viễn, hoặc nếu có bất kỳ lỗi nào xảy ra thì toàn bộ dữ liệu tạm thời đều phải bị hủy bỏ hoàn toàn (Rollback).
2. **Yêu cầu kỹ thuật cụ thể theo đề bài:**
   - Mở và cập nhật ứng dụng Web quản lý User.
   - Cập nhật phương thức `addUserTransaction()` trong lớp `UserDAO`.
   - **Sửa đổi mã nguồn để cố tình gây ra lỗi SQL** (ví dụ câu lệnh chèn vào bảng `user_permission` bị sai cú pháp, thừa tham số hoặc sai tên cột).
   - Chạy chương trình, quan sát kết quả xử lý và kiểm tra dữ liệu trong cơ sở dữ liệu MySQL:
     - **Khi có lỗi SQL:** Ngoại lệ `SQLException` được ném ra $\rightarrow$ khối `catch` gọi `connection.rollback()` $\rightarrow$ bản ghi `User` đã thêm ở bước trước bị hủy bỏ, **không hề xuất hiện trong bảng `users`**.
     - **Khi chạy đúng (chuẩn):** Thực thi trơn tru $\rightarrow$ gọi `connection.commit()` $\rightarrow$ cả `User` và các quyền trong `user_permission` đều được ghi nhận vĩnh viễn vào CSDL.

---

## 🏗️ 2. Cấu Trúc Dự Án (BTH6-BE13)

```text
BTH6-BE13/
├── .vscode/
│   ├── settings.json                   # Cấu hình UTF-8 bắt buộc cho VS Code
│   └── tasks.json                      # Tasks tự động: Build Maven, Deploy Tomcat, Run Test, Query MySQL
├── database/
│   ├── setup_demo_db.sql               # Script khởi tạo CSDL 'demo' và bảng 'users'
│   └── setup_transaction_db.sql        # Script tạo bảng 'permission' và 'user_permission'
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── codegym/
│       │           ├── controller/
│       │           │   └── UserServlet.java        # Tiếp nhận routing: create, test-rollback, test-commit
│       │           ├── dao/
│       │           │   ├── IUserDAO.java           # Khai báo addUserTransaction()
│       │           │   └── UserDAO.java            # Triển khai commit() & rollback() kèm mô phỏng lỗi SQL
│       │           ├── model/
│       │           │   └── User.java               # Entity User kèm danh sách permissions
│       │           └── test/
│       │               └── TestTransactionApp.java # Ứng dụng Console kiểm thử độc lập 2 kịch bản
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml                         # Deployment Descriptor Jakarta EE 10
│           ├── index.jsp                           # Trang chủ giới thiệu bài tập và các nút kiểm thử
│           └── user/
│               ├── create.jsp                      # Form thêm User kèm Checkbox "Cố tình gây lỗi SQL"
│               ├── edit.jsp                        # Form chỉnh sửa User
│               └── list.jsp                        # Danh sách Users kèm Banner thông báo Rollback/Commit
├── deploy.bat                                      # Script tự động đóng gói .war và triển khai lên Tomcat
└── pom.xml                                         # Cấu hình Maven (Jakarta Servlet 6.0, MySQL Connector/J 8.4)
```

---

## 💻 3. Chi Tiết Sửa Đổi Mã Nguồn Trong `UserDAO.java`

Mã nguồn phương thức `addUserTransaction()` được triển khai cụ thể như sau:

```java
@Override
public void addUserTransaction(User user, int[] permissionIds, boolean simulateSqlError) throws SQLException {
    Connection connection = null;
    PreparedStatement pstmtUser = null;
    PreparedStatement pstmtAssignment = null;
    ResultSet rs = null;

    try {
        connection = getConnection();
        // 1. Tắt chế độ tự động commit để bắt đầu một Transaction
        connection.setAutoCommit(false);

        // BƯỚC 1: Thêm mới người dùng vào bảng 'users'
        String insertUsersSql = "INSERT INTO users (name, email, country) VALUES(?, ?, ?)";
        pstmtUser = connection.prepareStatement(insertUsersSql, Statement.RETURN_GENERATED_KEYS);
        pstmtUser.setString(1, user.getName());
        pstmtUser.setString(2, user.getEmail());
        pstmtUser.setString(3, user.getCountry());
        pstmtUser.executeUpdate();

        rs = pstmtUser.getGeneratedKeys();
        int userId = 0;
        if (rs.next()) {
            userId = rs.getInt(1);
        }
        System.out.println("[JDBC TRANSACTION] Bước 1: Đã INSERT User tạm thời (id=" + userId + ") - Chưa commit!");

        // BƯỚC 2: Thêm các quyền của user vào bảng trung gian 'user_permission'
        if (permissionIds != null && permissionIds.length > 0) {
            String insertPermissionSql;
            if (simulateSqlError) {
                // =========================================================================
                // SỬA ĐỔI MÃ NGUỒN ĐỂ GÂY RA LỖI SQL THEO ĐỀ BÀI:
                // Cố tình tạo câu lệnh SQL sai (chèn vào cột không tồn tại / thừa tham số)
                // =========================================================================
                System.out.println("[JDBC TRANSACTION] ⚠️ Đang thực thi câu lệnh SQL cố tình lỗi...");
                insertPermissionSql = "INSERT INTO user_permission (user_id, permission_id, cot_khong_ton_tai_gay_loi) VALUES(?, ?, ?)";
            } else {
                // Câu lệnh SQL chuẩn xác
                insertPermissionSql = "INSERT INTO user_permission (user_id, permission_id) VALUES(?, ?)";
            }

            pstmtAssignment = connection.prepareStatement(insertPermissionSql);
            for (int permissionId : permissionIds) {
                pstmtAssignment.setInt(1, userId);
                pstmtAssignment.setInt(2, permissionId);
                pstmtAssignment.executeUpdate(); // Nếu simulateSqlError=true -> Ném SQLException tại đây!
            }
        }

        // BƯỚC 3: Nếu tất cả thành công, COMMIT toàn bộ giao dịch
        connection.commit();
        System.out.println(">>> [SUCCESS] Transaction đã được COMMIT thành công!");

    } catch (SQLException e) {
        System.err.println(">>> [ERROR] Phát hiện ngoại lệ: " + e.getMessage());
        try {
            if (connection != null) {
                // GỌI ROLLBACK ĐỂ HỦY BỎ TOÀN BỘ CÁC THAO TÁC TRƯỚC ĐÓ
                connection.rollback();
                System.err.println(">>> [ROLLBACK] connection.rollback() đã thực thi thành công!");
                System.err.println(">>> [KẾT QUẢ] Bước 1 đã bị thu hồi! Bảng users KHÔNG có user mới!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        throw e; // Ném tiếp ngoại lệ để tầng Servlet thông báo lên Web UI
    } finally {
        // Đóng các Statement, ResultSet và khôi phục autoCommit(true)
        if (connection != null) {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
}
```

---

## 🚀 4. Hướng Dẫn Kiểm Thử & Quan Sát Kết Quả

Bạn có thể kiểm tra kết quả theo 2 cách cực kỳ tiện lợi:

### Cách 1: Chạy Kiểm Thử Nhanh Qua Console (`TestTransactionApp`)

Chạy lệnh sau trong terminal hoặc dùng VS Code Task:
```powershell
java -cp "target\classes;target\user-management\WEB-INF\lib\*" com.codegym.test.TestTransactionApp
```

**Kết quả Console hiển thị rõ ràng 2 kịch bản:**
1. **Kịch bản 1 (Cố tình gây lỗi SQL):**
   - Bước 1: `INSERT INTO users` tạm thời.
   - Bước 2: Ném ngoại lệ `SQLException: No value specified for parameter 3`.
   - Bắt lỗi $\rightarrow$ gọi `connection.rollback()`.
   - Kiểm tra MySQL: `SELECT COUNT(*) FROM users WHERE name = 'User_Simulate_Error_...'` $\rightarrow$ **Kết quả = 0 (✅ THÀNH CÔNG, dữ liệu đã bị Rollback hoàn toàn)**.
2. **Kịch bản 2 (Chạy bình thường):**
   - Thực thi trơn tru cả 2 bước $\rightarrow$ gọi `connection.commit()`.
   - Kiểm tra MySQL: `SELECT COUNT(*) FROM users WHERE name = 'User_Commit_Ok_...'` $\rightarrow$ **Kết quả = 1 (✅ THÀNH CÔNG, đã lưu vĩnh viễn)**.

---

### Cách 2: Kiểm Thử Trên Giao Diện Web (Tomcat)

1. **Khởi chạy ứng dụng:**
   - Chạy file `deploy.bat` hoặc mở trình duyệt tại:
     `http://localhost:8080/user-management/users`
2. **Kiểm tra trên Web:**
   - **Thử nghiệm Rollback:** Bấm nút màu đỏ **"❌ Test 1: Mô phỏng lỗi SQL (Rollback)"** $\rightarrow$ Web hiển thị Banner đỏ thông báo Transaction đã Rollback, kiểm tra bảng danh sách thấy không có user rác nào.
   - **Thử nghiệm Commit:** Bấm nút màu xanh **"✅ Test 2: Chạy bình thường (Commit)"** $\rightarrow$ Web hiển thị Banner xanh thông báo Commit thành công, user mới xuất hiện trong bảng cùng các quyền hạn đã chọn.
   - **Thêm mới User tùy biến:** Vào mục `+ Thêm Mới User`, nhập tên, tích chọn các quyền, và có thể tích vào ô **"⚠️ Cố tình gây lỗi SQL (Test Rollback theo đề bài)"** để quan sát hành vi Rollback trực tiếp từ Form.

---

### Cách 3: Kiểm Tra Trực Tiếp Trong MySQL Database

Mở MySQL Command Line hoặc MySQL Workbench và chạy câu lệnh:
```sql
USE demo;

-- Xem danh sách người dùng và các quyền đã được commit
SELECT u.id, u.name, u.email, u.country, GROUP_CONCAT(p.name SEPARATOR ', ') AS permissions
FROM users u
LEFT JOIN user_permission up ON u.id = up.user_id
LEFT JOIN permission p ON up.permission_id = p.id
GROUP BY u.id;
```

---

## 📌 5. Kết Luận Bài Học

- Khi không có Transaction (`autoCommit = true`), nếu câu lệnh sau bị lỗi, các câu lệnh trước đó vẫn nằm lại trong CSDL gây ra hiện tượng **"rác dữ liệu"** hoặc **"sai lệch nghiệp vụ"**.
- Khi sử dụng JDBC Transaction (`autoCommit = false`, `commit()`, `rollback()`):
  - Dữ liệu chỉ thực sự được ghi nhận khi gọi `commit()`.
  - Bất kỳ khi nào xảy ra lỗi trong khối `try`, phương thức `rollback()` trong khối `catch` sẽ đưa CSDL trở về nguyên vẹn trạng thái ban đầu, bảo đảm 100% tính **Atomicity (Nguyên tử)**.
