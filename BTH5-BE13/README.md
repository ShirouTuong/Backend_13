# [Bài Tập] Gọi MySQL Stored Procedures từ JDBC (BTH5-BE13)

Dự án thực hành nâng cao tích hợp **MySQL Stored Procedures** vào ứng dụng Web Quản lý Người dùng (**User Management**) sử dụng **Java Servlet (Jakarta EE 10)**, **JDBC `CallableStatement`**, **MySQL 8.0**, và triển khai trên máy chủ **Apache Tomcat 10.1**.

---

## 🎯 Mục Tiêu & Yêu Cầu Bài Tập

1. **Luyện tập sử dụng Stored Procedures với JDBC:**
   - Thay thế các câu truy vấn SQL thông thường bằng các thủ tục lưu trữ biên dịch sẵn (**Stored Procedures**) trong MySQL Database.
   - Sử dụng đối tượng `CallableStatement` của Java JDBC để thực thi Stored Procedures.
2. **Triển khai đầy đủ các chức năng theo yêu cầu đề bài:**
   - **Hiển thị danh sách users:** Gọi Stored Procedure `get_all_users()` thông qua `CallableStatement`.
   - **Sửa thông tin user:** Gọi Stored Procedure `update_user(user_id, name, email, country)` thông qua `CallableStatement`.
   - **Xoá user:** Gọi Stored Procedure `delete_user(user_id)` thông qua `CallableStatement`.
   - **Lấy thông tin theo ID & Thêm mới:** Tích hợp với `get_user_by_id(id)` và `insert_user(name, email, country)` để hoàn thiện trọn vẹn chu trình CRUD 100% bằng Stored Procedures!

---

## 📁 Cấu Trúc Dự Án (BTH5-BE13)

```text
BTH5-BE13/
├── .vscode/
│   ├── settings.json                   # Cấu hình UTF-8 bắt buộc và môi trường VS Code
│   └── tasks.json                      # Tasks tự động: Build, Deploy, Run MySQL script
├── database/
│   ├── create_procedures.sql           # Script định nghĩa toàn bộ 5 Stored Procedures
│   └── setup_demo_db.sql               # Script khởi tạo CSDL 'demo', bảng 'users' và nạp dữ liệu mẫu
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── codegym/
│       │           ├── controller/
│       │           │   └── UserServlet.java  # Controller điều hướng: list, edit, delete, create
│       │           ├── dao/
│       │           │   ├── IUserDAO.java     # Interface định nghĩa các phương thức nghiệp vụ & Store
│       │           │   └── UserDAO.java      # Hiện thực CallableStatement gọi Stored Procedures
│       │           └── model/
│       │               └── User.java         # Entity biểu diễn đối tượng người dùng
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml                   # Cấu hình Deployment Descriptor Jakarta EE
│           ├── index.jsp                     # Trang chủ giới thiệu bài tập và các thủ tục
│           └── user/
│               ├── create.jsp                # Form thêm mới user (insert_user)
│               ├── edit.jsp                  # Form cập nhật thông tin user (update_user)
│               └── list.jsp                  # Bảng danh sách người dùng (get_all_users, delete_user)
├── deploy.bat                          # Script đóng gói .war và triển khai tự động lên Tomcat 10
├── pom.xml                             # Cấu hình Maven dependencies (Jakarta Servlet, JSTL, MySQL Connector/J)
└── README.md                           # Tài liệu hướng dẫn chi tiết bài tập
```

---

## 🗄️ Chi Tiết Các MySQL Stored Procedures (Bước 1)

File: [`database/create_procedures.sql`](file:///C:/Users/ADMIN/BTH5-BE13/database/create_procedures.sql)

### 1. Hiển thị danh sách tất cả Users (`get_all_users`)
```sql
DELIMITER $$
CREATE PROCEDURE get_all_users()
BEGIN
    SELECT id, name, email, country 
    FROM users 
    ORDER BY id ASC;
END$$
DELIMITER ;
```

### 2. Cập nhật thông tin User (`update_user`)
```sql
DELIMITER $$
CREATE PROCEDURE update_user(
    IN user_id INT,
    IN user_name VARCHAR(120),
    IN user_email VARCHAR(220),
    IN user_country VARCHAR(120)
)
BEGIN
    UPDATE users
    SET name = user_name,
        email = user_email,
        country = user_country
    WHERE id = user_id;
END$$
DELIMITER ;
```

### 3. Xóa User theo ID (`delete_user`)
```sql
DELIMITER $$
CREATE PROCEDURE delete_user(IN user_id INT)
BEGIN
    DELETE FROM users 
    WHERE id = user_id;
END$$
DELIMITER ;
```

### 4. Lấy thông tin User theo ID (`get_user_by_id`)
```sql
DELIMITER $$
CREATE PROCEDURE get_user_by_id(IN user_id INT)
BEGIN
    SELECT users.name, users.email, users.country
    FROM users
    WHERE users.id = user_id;
END$$
DELIMITER ;
```

### 5. Thêm mới User (`insert_user`)
```sql
DELIMITER $$
CREATE PROCEDURE insert_user(
    IN user_name VARCHAR(120),
    IN user_email VARCHAR(220),
    IN user_country VARCHAR(120)
)
BEGIN
    INSERT INTO users(name, email, country)
    VALUES(user_name, user_email, user_country);
END$$
DELIMITER ;
```

---

## ☕ Triển Khai Mã Nguồn Java JDBC (Bước 2 & 3 & 4)

### 1. Cập nhật Interface `IUserDAO` ([IUserDAO.java](file:///C:/Users/ADMIN/BTH5-BE13/src/main/java/com/codegym/dao/IUserDAO.java))
Khai báo đầy đủ các phương thức sử dụng Stored Procedures:
- `List<User> selectAllUsersStore();`
- `boolean updateUserStore(User user) throws SQLException;`
- `boolean deleteUserStore(int id) throws SQLException;`
- `User getUserById(int id);`
- `void insertUserStore(User user) throws SQLException;`

### 2. Cập nhật Lớp `UserDAO` ([UserDAO.java](file:///C:/Users/ADMIN/BTH5-BE13/src/main/java/com/codegym/dao/UserDAO.java))
Hiện thực logic thông qua `connection.prepareCall("{CALL procedure_name(?)}")`:

```java
// Gọi get_all_users()
String query = "{CALL get_all_users()}";
try (Connection connection = getConnection();
     CallableStatement callableStatement = connection.prepareCall(query);
     ResultSet rs = callableStatement.executeQuery()) {
    while (rs.next()) {
        users.add(new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("country")));
    }
}

// Gọi update_user(?, ?, ?, ?)
String query = "{CALL update_user(?, ?, ?, ?)}";
try (Connection connection = getConnection();
     CallableStatement callableStatement = connection.prepareCall(query)) {
    callableStatement.setInt(1, user.getId());
    callableStatement.setString(2, user.getName());
    callableStatement.setString(3, user.getEmail());
    callableStatement.setString(4, user.getCountry());
    rowUpdated = callableStatement.executeUpdate() > 0;
}

// Gọi delete_user(?)
String query = "{CALL delete_user(?)}";
try (Connection connection = getConnection();
     CallableStatement callableStatement = connection.prepareCall(query)) {
    callableStatement.setInt(1, id);
    rowDeleted = callableStatement.executeUpdate() > 0;
}
```

### 3. Cập nhật `UserServlet` ([UserServlet.java](file:///C:/Users/ADMIN/BTH5-BE13/src/main/java/com/codegym/controller/UserServlet.java))
- Phương thức `listUser()`: Gọi `userDAO.selectAllUsersStore()` lấy danh sách nạp vào `user/list.jsp`.
- Phương thức `updateUser()`: Gọi `userDAO.updateUserStore(userToUpdate)` xử lý form sửa thông tin.
- Phương thức `deleteUser()`: Gọi `userDAO.deleteUserStore(id)` xóa người dùng.

---

## 🚀 Hướng Dẫn Vận Hành & Kiểm Thử (Bước 5)

### Cách 1: Sử dụng file `deploy.bat`
Chạy file script triển khai tự động:
```powershell
.\deploy.bat
```
Script sẽ tự động:
1. Dọn dẹp và biên dịch ứng dụng bằng Maven.
2. Sao chép `target/user-management.war` vào thư mục `webapps` của Apache Tomcat.
3. Khởi động Tomcat Server.

### Cách 2: Sử dụng VS Code Tasks
Nhấn tổ hợp phím `Ctrl + Shift + P` -> gõ `Tasks: Run Task`:
- **`MySQL: Setup Demo DB and Procedures`**: Tạo CSDL và các Stored Procedure vào MySQL.
- **`Maven: Build Package`**: Biên dịch mã nguồn Java.
- **`Deploy to Tomcat`**: Đóng gói và chạy trên Tomcat.
- **`MySQL: Test Call get_all_users`**: Kiểm tra thủ tục hiển thị danh sách từ terminal.

### Đường dẫn truy cập:
- **Trang chủ giới thiệu bài tập:** [http://localhost:8080/user-management/](http://localhost:8080/user-management/)
- **Trang danh sách người dùng:** [http://localhost:8080/user-management/users](http://localhost:8080/user-management/users)
- **Thêm mới người dùng:** [http://localhost:8080/user-management/users?action=create](http://localhost:8080/user-management/users?action=create)
- **Chỉnh sửa thông tin người dùng:** [http://localhost:8080/user-management/users?action=edit&id=1](http://localhost:8080/user-management/users?action=edit&id=1)
