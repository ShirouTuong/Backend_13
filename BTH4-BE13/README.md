# BÀI THỰC HÀNH 4: THỰC THI SQL CÓ SỬ DỤNG JDBC TRANSACTION
## (All-or-Nothing với MySQL Workbench và VS Code)

---

## 🎯 Mục Tiêu Bài Học
- Hiểu và vận dụng thành thạo **JDBC Transaction** để kiểm soát quá trình ghi dữ liệu vào hệ quản trị cơ sở dữ liệu MySQL.
- Đảm bảo tuân thủ nguyên lý **Atomicity (All-or-Nothing)** trong bộ tiêu chuẩn **ACID**:
  - **Trường hợp 1 (Thành công trọn vẹn):** Toàn bộ các thao tác `INSERT`, `UPDATE` trong khối giao dịch được ghi nhận vĩnh viễn vào CSDL thông qua lệnh `conn.commit()`.
  - **Trường hợp 2 (Phát sinh lỗi):** Nếu có bất kỳ câu lệnh nào thất bại (văng ngoại lệ), toàn bộ dữ liệu tạm thời chưa commit sẽ bị JDBC tự động hủy bỏ (**Rollback**). Cơ sở dữ liệu trở về nguyên vẹn trạng thái ban đầu, hoàn toàn không để lại rác dữ liệu.
- Cấu hình chuẩn tiếng Việt (**UTF-8 / utf8mb4**) trên toàn bộ hệ thống: **VS Code, MySQL Workbench, Maven compiler, Jakarta Servlet, JSP**.

---

## 📁 Cấu Trúc Dự Án (chuẩn Maven + VS Code)
```text
BTH4-BE13/
├── .vscode/
│   ├── settings.json       # Cấu hình mã hóa UTF-8 bắt buộc cho VS Code
│   └── tasks.json          # Các tác vụ chạy nhanh: Build Maven, Deploy Tomcat, kiểm tra MySQL
├── database/
│   ├── setup_employee.sql  # Script tạo bảng Employee với charset utf8mb4
│   ├── setup_demo_db.sql   # Script khởi tạo cơ sở dữ liệu demo và users
│   └── setup_transaction_db.sql # Script bảng permission và user_permission
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── codegym/
│       │           ├── controller/
│       │           │   └── UserServlet.java  # Tiếp nhận routing: action=test-use-tran
│       │           ├── dao/
│       │           │   ├── IUserDAO.java     # Khai báo insertUpdateUseTransaction()
│       │           │   └── UserDAO.java      # Triển khai Transaction: setAutoCommit(false), commit()
│       │           └── model/
│       │               └── User.java         # Lớp đối tượng User
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml                   # Cấu hình Servlet Container
│           ├── index.jsp                     # Trang giới thiệu bài tập, giao diện đẹp mắt
│           └── user/
│               ├── list.jsp                  # Giao diện danh sách người dùng & nút chạy kiểm thử
│               ├── create.jsp                # Giao diện thêm mới user
│               └── edit.jsp                  # Giao diện cập nhật user
├── pom.xml                                   # File cấu hình thư viện Maven & build WAR
└── deploy.bat                                # Script đóng gói và triển khai tự động lên Tomcat
```

---

## 🛠️ Hướng Dẫn Thực Hiện Từng Bước (Theo đúng đề bài)

### Bước 1: Cập nhật Interface `IUserDAO.java`
Mở file [IUserDAO.java](file:///c:/Users/ADMIN/BTH4-BE13/src/main/java/com/codegym/dao/IUserDAO.java) và bổ sung thêm phương thức:
```java
// Bước 1: Khai báo phương thức thực thi có dùng Transaction
public void insertUpdateUseTransaction();
public void insertUpdateUseTransaction(boolean fixBug);
```

### Bước 2: Cập nhật lớp `UserDAO.java`
Mở file [UserDAO.java](file:///c:/Users/ADMIN/BTH4-BE13/src/main/java/com/codegym/dao/UserDAO.java) và triển khai phương thức `insertUpdateUseTransaction()`:
```java
@Override
public void insertUpdateUseTransaction() {
    insertUpdateUseTransaction(false); // Mặc định chạy Lần 1: Cố tình gây lỗi để kiểm tra Rollback
}

@Override
public void insertUpdateUseTransaction(boolean fixBug) {
    try (Connection conn = getConnection();
            Statement statement = conn.createStatement();
            PreparedStatement psInsert = conn.prepareStatement(SQL_INSERT);
            PreparedStatement psUpdate = conn.prepareStatement(SQL_UPDATE)) {

        statement.execute(SQL_TABLE_DROP);
        statement.execute(SQL_TABLE_CREATE);

        // ----------------------------------------------------
        // 1. BẮT ĐẦU TRANSACTION BLOCK
        // Tắt chế độ lưu tự động (mặc định là true)
        // ----------------------------------------------------
        conn.setAutoCommit(false);

        // 2. Chạy danh sách lệnh Insert
        psInsert.setString(1, "Quynh");
        psInsert.setBigDecimal(2, new BigDecimal(10));
        psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        psInsert.execute();

        psInsert.setString(1, "Ngan");
        psInsert.setBigDecimal(2, new BigDecimal(20));
        psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        psInsert.execute();

        // 3. Chạy lệnh Update
        if (!fixBug) {
            // Lần 1: Cố tình tạo lỗi để kiểm thử (index 2 vượt quá số lượng tham số)
            psUpdate.setBigDecimal(2, new BigDecimal(999.99));
        } else {
            // Lần 2: Sửa lỗi đúng chuẩn (tham số salary là index 1)
            psUpdate.setBigDecimal(1, new BigDecimal(999.99));
        }

        psUpdate.setString(2, "Quynh");
        psUpdate.execute();

        // ----------------------------------------------------
        // 4. KẾT THÚC TRANSACTION BLOCK
        // Nếu chạy trót lọt đến đây, tiến hành ghi vĩnh viễn vào CSDL
        // ----------------------------------------------------
        conn.commit();

        // Bật lại chế độ auto-commit về mặc định
        conn.setAutoCommit(true);
        System.out.println("Transaction đã được commit thành công! Dữ liệu đã được lưu.");

    } catch (Exception e) {
        System.out.println("Lỗi xảy ra, Transaction sẽ tự động huỷ bỏ (rollback)...");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}
```

### Bước 3: Cập nhật `UserServlet.java`
Mở file [UserServlet.java](file:///c:/Users/ADMIN/BTH4-BE13/src/main/java/com/codegym/controller/UserServlet.java):
1. Thêm case vào `switch (action)` trong `doGet()`:
```java
case "test-use-tran":
    testUseTran(request, response);
    break;
```
2. Triển khai phương thức `testUseTran()`:
```java
private void testUseTran(HttpServletRequest request, HttpServletResponse response) {
    String fixParam = request.getParameter("fix");
    boolean isFixed = "true".equalsIgnoreCase(fixParam);

    userDAO.insertUpdateUseTransaction(isFixed);
    System.out.println("Hoàn tất gọi hàm testUseTran! (Chế độ sửa lỗi: " + isFixed + ")");
    // Hiển thị kết quả báo cáo trực quan trên trình duyệt (hỗ trợ tiếng Việt UTF-8 chuẩn)
}
```

---

## 🚀 Bước 4: Chạy Ứng Dụng & Kiểm Thử Trên MySQL Workbench

### 🔴 LẦN 1: Chạy Với Mã Cố Tình Gây Lỗi (Kiểm tra Rollback)
1. **Truy cập đường dẫn trên trình duyệt:**
   [http://localhost:8080/user-management/users?action=test-use-tran](http://localhost:8080/user-management/users?action=test-use-tran)
2. **Kiểm tra Log Console của IDE / Tomcat:**
   Xuất hiện thông báo bắt lỗi và ngoại lệ:
   ```text
   Lỗi xảy ra, Transaction sẽ tự động huỷ bỏ (rollback)...
   Parameter index out of range (2 > number of parameters, which is 2)
   Hoàn tất gọi hàm testUseTran!
   ```
3. **Mở MySQL Workbench và thực hiện câu lệnh:**
   ```sql
   USE demo;
   SELECT * FROM Employee;
   ```
4. **Kết quả thu được thực tế:**
   ```text
   Bảng trống trơn (0 bản ghi)!
   ```
   👉 **Giải thích:** Dù 2 lệnh Insert (`Quynh` lương 10, `Ngan` lương 20) đã chạy qua, nhưng khi câu lệnh Update phía sau bị lỗi, chương trình nhảy vào khối `catch`, lệnh `conn.commit()` **không bao giờ được gọi**. Khi kết nối đóng, JDBC tự động hủy bỏ (rollback) toàn bộ các thao tác trước đó. **Rác dữ liệu đã được ngăn chặn triệt để!**

---

### 🟢 LẦN 2: Sửa Lỗi Để Ứng Dụng Hoạt Động Chuẩn Xác (Kiểm tra Commit)
1. **Sửa dòng code trong `UserDAO.java`:**
   Chuyển chỉ số tham số từ `2` thành `1`:
   ```java
   // Dòng gây lỗi: psUpdate.setBigDecimal(2, new BigDecimal(999.99));
   // Sửa thành:
   psUpdate.setBigDecimal(1, new BigDecimal(999.99));
   ```
2. **Truy cập đường dẫn kiểm thử Lần 2:**
   [http://localhost:8080/user-management/users?action=test-use-tran&fix=true](http://localhost:8080/user-management/users?action=test-use-tran&fix=true)
   *(Hoặc click nút "Chạy Thử Lần 2: Sửa Đúng (Commit)" trên giao diện web)*.
3. **Kiểm tra Log Console của IDE / Tomcat:**
   ```text
   Transaction đã được commit thành công! Dữ liệu đã được lưu.
   Hoàn tất gọi hàm testUseTran!
   ```
4. **Mở MySQL Workbench và thực hiện truy vấn:**
   ```sql
   USE demo;
   SELECT * FROM Employee;
   ```
5. **Kết quả thu được thực tế trong MySQL Workbench:**
   ```text
   +----+-------+--------+---------------------+
   | id | name  | salary | created_date        |
   +----+-------+--------+---------------------+
   |  1 | Quynh | 999.99 | 2026-10-14 22:33:00 |
   |  2 | Ngan  |  20.00 | 2026-10-14 22:33:00 |
   +----+-------+--------+---------------------+
   2 rows in set
   ```
   👉 **Giải thích:** Tất cả các câu lệnh Insert và Update đều thực thi thành công và được xác nhận vĩnh viễn bằng một lệnh `conn.commit()` duy nhất. Quynh đã được cập nhật mức lương mới là `999.99`.

---

## 🔤 Bí Quyết Đảm Bảo Không Bao Giờ Bị Lỗi Phông Chữ Tiếng Việt
1. **Database:** Dùng `CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci` trên Database và từng bảng (`users`, `Employee`, `permission`).
2. **JDBC Connection String:** Thêm tham số `characterEncoding=UTF-8&useUnicode=true`.
3. **Servlet:** Đặt encoding ngay đầu `doGet()` và `doPost()`:
   ```java
   request.setCharacterEncoding("UTF-8");
   response.setContentType("text/html; charset=UTF-8");
   response.setCharacterEncoding("UTF-8");
   ```
4. **JSP Files:** Đầu mỗi file JSP phải có chỉ thị:
   ```jsp
   <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
   <meta charset="UTF-8">
   ```
5. **Maven pom.xml:** Đặt `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>` và cấu hình encoding cho `maven-compiler-plugin`.
6. **VS Code:** File `.vscode/settings.json` cấu hình `"files.encoding": "utf8"`.
