# [Thực hành] MySQL JDBC Transaction

Dự án thực hành quản lý người dùng nâng cao (User Management) sử dụng **Java Servlet**, **JDBC Transaction**, và **MySQL** trong **Visual Studio Code** và **MySQL Workbench**.

Mục tiêu cốt lõi: Sử dụng các phương thức `connection.setAutoCommit(false)`, `connection.commit()`, và `connection.rollback()` để đảm bảo tính toàn vẹn dữ liệu (ACID) khi chèn đồng thời vào bảng `users` và bảng quan hệ nhiều-nhiều `user_permission`.

---

## 1. Cấu trúc Dự án trong VS Code

```text
BTH2-BE13/
├── .vscode/
│   └── tasks.json                     # Cấu hình tác vụ Maven Build & Run Script trong VS Code
├── database/
│   ├── setup_transaction_db.sql       # Script tạo bảng permission, user_permission và dữ liệu mẫu
│   └── setup_demo_db.sql              # Script khởi tạo cơ sở dữ liệu demo ban đầu
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── codegym/
│       │           ├── controller/
│       │           │   └── UserServlet.java    # Xử lý nhận mảng permissions và gọi DAO
│       │           ├── dao/
│       │           │   ├── IUserDAO.java       # Khai báo addUserTransaction(user, permissionIds)
│       │           │   └── UserDAO.java        # Thực thi JDBC Transaction (commit & rollback)
│       │           └── model/
│       │               └── User.java           # Model User với thuộc tính permissions
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml                     # Cấu hình servlet mapping
│           ├── user/
│           │   ├── create.jsp                  # Giao diện Form thêm mới kèm Checkbox quyền hạn
│           │   ├── edit.jsp                    # Form chỉnh sửa User
│           │   └── list.jsp                    # Danh sách User kèm hiển thị quyền hạn (Permissions)
│           └── index.jsp                       # Trang chủ điều hướng dự án
├── deploy.bat                                  # Script tự động build Maven và triển khai lên Tomcat
└── pom.xml                                     # Cấu hình Maven (Jakarta EE 10, MySQL Connector/J)
```

---

## 2. Hướng dẫn Thực hiện Chi tiết

### Bước 1: Cập nhật Cơ sở Dữ liệu trong MySQL Workbench

1. Mở **MySQL Workbench**, chọn kết nối tới MySQL Server cục bộ (ví dụ: `Local instance MySQL80` với user `root`).
2. Mở tab truy vấn mới (**File** -> **New Query Tab** hoặc tổ hợp phím `Ctrl + T`).
3. Dán và thực thi đoạn mã SQL sau:

```sql
USE demo;

-- 1. Tạo bảng Permission (Danh mục quyền)
CREATE TABLE IF NOT EXISTS permission (
    id INT(11) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- 2. Tạo bảng trung gian User_Permission (Liên kết người dùng - quyền)
CREATE TABLE IF NOT EXISTS user_permission (
    user_id INT(11) NOT NULL,
    permission_id INT(11) NOT NULL,
    PRIMARY KEY(user_id, permission_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- 3. Thêm dữ liệu mẫu vào bảng Permission
INSERT IGNORE INTO permission(id, name) VALUES (1, 'add');
INSERT IGNORE INTO permission(id, name) VALUES (2, 'edit');
INSERT IGNORE INTO permission(id, name) VALUES (3, 'delete');
INSERT IGNORE INTO permission(id, name) VALUES (4, 'view');
```

4. Nhấn biểu tượng tia sét ⚡ (**Execute**) để chạy lệnh.
5. Kiểm tra bảng bằng truy vấn:
```sql
SELECT * FROM permission;
```

---

### Bước 2: Cập nhật Interface `IUserDAO.java`

Mở file `src/main/java/com/codegym/dao/IUserDAO.java` trong VS Code và bổ sung khai báo phương thức:

```java
package com.codegym.dao;

import com.codegym.model.User;
import java.sql.SQLException;
import java.util.List;

public interface IUserDAO {
    // Các phương thức cơ bản CRUD...
    public void insertUser(User user) throws SQLException;
    public User selectUser(int id);
    public List<User> selectAllUsers();
    public boolean deleteUser(int id) throws SQLException;
    public boolean updateUser(User user) throws SQLException;
    public User getUserById(int id);
    public void insertUserStore(User user) throws SQLException;

    // THÊM MỚI PHƯƠNG THỨC XỬ LÝ TRANSACTION
    public void addUserTransaction(User user, int[] permissionIds) throws SQLException;

    // Lấy danh sách tên quyền của User
    public List<String> getUserPermissions(int userId);
}
```

---

### Bước 3: Triển khai JDBC Transaction trong `UserDAO.java`

Mở file `src/main/java/com/codegym/dao/UserDAO.java` và triển khai phương thức `addUserTransaction`:

**Nguyên lý vận hành của Transaction:**
1. `connection.setAutoCommit(false)`: Chuyển kết nối sang chế độ thủ công, bắt đầu một transaction.
2. `PreparedStatement` với cờ `Statement.RETURN_GENERATED_KEYS`: Cho phép lấy lại ID tự sinh sau khi thêm vào bảng `users`.
3. Chèn các bản ghi vào bảng `user_permission` tương ứng với `userId` vừa lấy được.
4. `connection.commit()`: Xác nhận và ghi vĩnh viễn dữ liệu vào CSDL khi tất cả bước trên thành công.
5. `connection.rollback()` trong khối `catch`: Hoàn tác toàn bộ các thay đổi nếu có bất kỳ ngoại lệ nào xảy ra, tránh sinh bản ghi rác trong bảng `users`.
6. Khối `finally`: Đảm bảo đóng các tài nguyên `ResultSet`, `PreparedStatement` và bật lại `connection.setAutoCommit(true)` trước khi đóng kết nối.

```java
    @Override
    public void addUserTransaction(User user, int[] permissionIds) throws SQLException {
        Connection connection = null;
        PreparedStatement pstmtUser = null;
        PreparedStatement pstmtAssignment = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            // 1. Tắt auto-commit để bắt đầu Transaction
            connection.setAutoCommit(false);

            // 2. Chèn dữ liệu vào bảng users và lấy ID vừa tạo
            String insertUsersSql = "INSERT INTO users (name, email, country) VALUES(?, ?, ?)";
            pstmtUser = connection.prepareStatement(insertUsersSql, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, user.getName());
            pstmtUser.setString(2, user.getEmail());
            pstmtUser.setString(3, user.getCountry());
            pstmtUser.executeUpdate();

            // 3. Lấy ID của user vừa được chèn
            rs = pstmtUser.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            // 4. Chèn dữ liệu vào bảng user_permission
            if (permissionIds != null && permissionIds.length > 0) {
                String insertPermissionSql = "INSERT INTO user_permission (user_id, permission_id) VALUES(?, ?)";
                pstmtAssignment = connection.prepareStatement(insertPermissionSql);

                for (int permissionId : permissionIds) {
                    pstmtAssignment.setInt(1, userId);
                    pstmtAssignment.setInt(2, permissionId);
                    pstmtAssignment.executeUpdate();
                }
            }

            // 5. Nếu mọi thao tác thành công, tiến hành Commit
            connection.commit();
            System.out.println("Transaction đã được commit thành công!");

        } catch (SQLException e) {
            // 6. Nếu có lỗi xảy ra ở bất kỳ đâu, Rollback lại toàn bộ dữ liệu
            try {
                if (connection != null) {
                    connection.rollback();
                    System.out.println("Có lỗi xảy ra! Transaction đã bị rollback.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw e;
        } finally {
            // 7. Dọn dẹp tài nguyên và bật lại auto-commit
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignored) {}
            }
            if (pstmtUser != null) {
                try { pstmtUser.close(); } catch (SQLException ignored) {}
            }
            if (pstmtAssignment != null) {
                try { pstmtAssignment.close(); } catch (SQLException ignored) {}
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {}
            }
        }
    }
```

---

### Bước 4: Cập nhật Giao diện `create.jsp` và Lớp `UserServlet.java`

#### 1. Cập nhật `create.jsp`
Mở `src/main/webapp/user/create.jsp`, thêm các checkbox phân quyền ngay phía trên nút Lưu thông tin:

```html
<div class="form-group">
    <label>Quyền hạn (Permissions):</label>
    <div style="display: flex; gap: 15px; margin-top: 5px; flex-wrap: wrap;">
        <label><input type="checkbox" name="permissions" value="1"> Thêm (Add)</label>
        <label><input type="checkbox" name="permissions" value="2"> Sửa (Edit)</label>
        <label><input type="checkbox" name="permissions" value="3"> Xoá (Delete)</label>
        <label><input type="checkbox" name="permissions" value="4"> Xem (View)</label>
    </div>
</div>
```

#### 2. Cập nhật `UserServlet.java`
Mở `src/main/java/com/codegym/controller/UserServlet.java`, chỉnh sửa hàm `insertUser`:

```java
    private void insertUser(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String country = request.getParameter("country");

        // Nhận danh sách quyền hạn (permissions) được check từ form
        String[] permissionsStr = request.getParameterValues("permissions");
        int[] permissions = null;

        if (permissionsStr != null) {
            permissions = new int[permissionsStr.length];
            for (int i = 0; i < permissionsStr.length; i++) {
                permissions[i] = Integer.parseInt(permissionsStr[i]);
            }
        }

        User newUser = new User(name, email, country);

        // Gọi phương thức mới sử dụng Transaction
        userDAO.addUserTransaction(newUser, permissions);

        // Quay lại trang danh sách sau khi lưu thành công
        response.sendRedirect("users");
    }
```

---

### Bước 5: Biên dịch và Chạy Ứng dụng

#### Cách 1: Sử dụng Terminal trong VS Code
Mở Terminal trong VS Code (`Ctrl + ` `) và chạy:
```powershell
.\deploy.bat
```
Script sẽ tự động:
1. Chạy `mvn clean package` để đóng gói `user-management.war`.
2. Triển khai file `.war` vào thư mục `webapps` của Apache Tomcat.
3. Khởi động máy chủ Tomcat.

#### Cách 2: Sử dụng VS Code Tasks
- Nhấn tổ hợp phím `Ctrl + Shift + B` để thực hiện Build package qua Maven.
- Mở menu **Terminal** -> **Run Task...** -> chọn **Deploy to Tomcat**.

#### Trải nghiệm trên trình duyệt:
1. Mở trình duyệt và truy cập:
   `http://localhost:8080/user-management/users?action=create`
2. Nhập thông tin:
   - **Họ và Tên**: `Nguyễn Văn A`
   - **Email**: `a@gmail.com`
   - **Quốc Gia**: `VN`
   - Tích chọn 2 quyền: **Thêm (Add)** và **Xem (View)**.
3. Bấm **Lưu thông tin**.
4. Ứng dụng tự động chuyển về trang danh sách (`http://localhost:8080/user-management/users`), người dùng vừa thêm sẽ xuất hiện kèm các thẻ quyền hạn `add` và `view`.

---

### Bước 6: Kiểm tra Kết quả trong MySQL Workbench

Mở **MySQL Workbench**, chạy 2 câu lệnh truy vấn sau:

#### 1. Kiểm tra bảng `users`:
```sql
USE demo;
SELECT * FROM users ORDER BY id DESC LIMIT 1;
```
*Kết quả:* Bản ghi người dùng vừa tạo xuất hiện ở dòng đầu tiên (ví dụ `id = 5`).

#### 2. Kiểm tra bảng `user_permission`:
```sql
SELECT * FROM user_permission WHERE user_id = 5;
```
*Kết quả:* Xuất hiện 2 dòng dữ liệu với `user_id = 5`:
- `permission_id = 1` (add)
- `permission_id = 4` (view)

#### 3. Kiểm tra liên kết quyền hạn (JOIN query):
```sql
SELECT u.id, u.name, u.email, p.name AS permission_name
FROM users u
JOIN user_permission up ON u.id = up.user_id
JOIN permission p ON up.permission_id = p.id
WHERE u.id = 5;
```

---

### Bước 7: Thử nghiệm Kiểm chứng Cơ chế Rollback

Để chứng minh tính năng của Transaction khi có lỗi xảy ra:
1. Trong file `UserDAO.java`, cố tình viết sai câu lệnh chèn vào bảng `user_permission` (ví dụ: `INSERT INTO user_permission_fail ...`).
2. Thực hiện thêm mới một người dùng từ form web.
3. Quan sát console Tomcat sẽ in ra thông báo:
   ```text
   Có lỗi xảy ra! Transaction đã bị rollback.
   ```
4. Mở MySQL Workbench kiểm tra lại bảng `users`: hoàn toàn **không có bất kỳ bản ghi rác nào** được thêm vào. Điều này chứng minh cơ chế Rollback đã hoạt động chính xác!
