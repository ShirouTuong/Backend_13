# [Thực hành] Gọi MySQL Stored Procedures từ JDBC

Bài thực hành cập nhật ứng dụng Web Quản lý Người dùng (User Management) sử dụng **Java Servlet**, **JDBC**, và **MySQL Stored Procedures** thông qua `CallableStatement`.

---

## 1. Cấu trúc Thư mục Dự án trong VS Code

```text
BTH1-BE13/
├── .vscode/
│   └── tasks.json                     # Các tác vụ Build & Deploy trong VS Code
├── database/
│   ├── create_procedures.sql          # Script tạo 2 Stored Procedures
│   └── setup_demo_db.sql              # Script tạo Database demo & Bảng users đầy đủ
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── codegym/
│       │           ├── controller/
│       │           │   └── UserServlet.java    # Controller điều hướng xử lý
│       │           ├── dao/
│       │           │   ├── IUserDAO.java       # Interface khai báo các hàm DAO
│       │           │   └── UserDAO.java        # Triển khai CallableStatement
│       │           └── model/
│       │               └── User.java           # Entity User (id, name, email, country)
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml            # Cấu hình deployment descriptor
│           ├── user/
│           │   ├── create.jsp         # Form thêm mới User (gọi insert_user)
│           │   ├── edit.jsp           # Form chỉnh sửa User (gọi get_user_by_id)
│           │   └── list.jsp           # Danh sách người dùng
│           └── index.jsp              # Trang chủ điều hướng
├── deploy.bat                         # Script tự động đóng gói Maven và deploy lên Tomcat
└── pom.xml                            # File cấu hình Maven (Jakarta EE 10, MySQL Connector/J)
```

---

## 2. Hướng dẫn chi tiết từng bước

### Bước 1: Định nghĩa Stored Procedures trên MySQL Workbench

Mở **MySQL Workbench**, kết nối tới MySQL Server (Local instance), mở tab Query và thực thi đoạn lệnh SQL:

```sql
USE demo;

-- 1. Tạo Stored Procedure để lấy thông tin User theo ID
DROP PROCEDURE IF EXISTS get_user_by_id;
DELIMITER $$
CREATE PROCEDURE get_user_by_id(IN user_id INT)
BEGIN
    SELECT users.name, users.email, users.country
    FROM users
    WHERE users.id = user_id;
END$$
DELIMITER ;

-- 2. Tạo Stored Procedure để thêm mới một User
DROP PROCEDURE IF EXISTS insert_user;
DELIMITER $$
CREATE PROCEDURE insert_user(
    IN user_name VARCHAR(50),
    IN user_email VARCHAR(50),
    IN user_country VARCHAR(50)
)
BEGIN
    INSERT INTO users(name, email, country)
    VALUES(user_name, user_email, user_country);
END$$
DELIMITER ;
```

> **Ghi chú**: Từ khóa `DELIMITER $$` dùng để đổi dấu phân cách kết thúc câu lệnh, tránh lỗi cú pháp khi MySQL đọc khối `BEGIN ... END`.

---

### Bước 2: Cập nhật Interface `IUserDAO.java`

Mở file [`src/main/java/com/codegym/dao/IUserDAO.java`](src/main/java/com/codegym/dao/IUserDAO.java) và bổ sung 2 phương thức:

```java
// THÊM MỚI 2 PHƯƠNG THỨC SỬ DỤNG STORED PROCEDURE
public User getUserById(int id);
public void insertUserStore(User user) throws SQLException;
```

---

### Bước 3: Triển khai Stored Procedure trong `UserDAO.java` (Sử dụng `CallableStatement`)

Mở file [`src/main/java/com/codegym/dao/UserDAO.java`](src/main/java/com/codegym/dao/UserDAO.java):

```java
// Triển khai hàm gọi Stored Procedure: get_user_by_id
@Override
public User getUserById(int id) {
    User user = null;
    String query = "{CALL get_user_by_id(?)}"; // Cú pháp chuẩn gọi Stored Procedure
    
    try (Connection connection = getConnection();
         CallableStatement callableStatement = connection.prepareCall(query)) {
        
        callableStatement.setInt(1, id);
        ResultSet rs = callableStatement.executeQuery();
        
        while (rs.next()) {
            String name = rs.getString("name");
            String email = rs.getString("email");
            String country = rs.getString("country");
            user = new User(id, name, email, country);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return user;
}

// Triển khai hàm gọi Stored Procedure: insert_user
@Override
public void insertUserStore(User user) throws SQLException {
    String query = "{CALL insert_user(?, ?, ?)}"; // 3 dấu ? tương ứng 3 tham số
    
    try (Connection connection = getConnection();
         CallableStatement callableStatement = connection.prepareCall(query)) {
        
        callableStatement.setString(1, user.getName());
        callableStatement.setString(2, user.getEmail());
        callableStatement.setString(3, user.getCountry());
        System.out.println(callableStatement);
        callableStatement.executeUpdate();
    }
}
```

---

### Bước 4: Cập nhật `UserServlet.java` (Tầng Controller)

Mở file [`src/main/java/com/codegym/controller/UserServlet.java`](src/main/java/com/codegym/controller/UserServlet.java):

1. **Cập nhật `showEditForm`**: Thay thế `userDAO.selectUser(id)` bằng `userDAO.getUserById(id)`:
```java
private void showEditForm(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, ServletException, IOException {
    int id = Integer.parseInt(request.getParameter("id"));
    
    // Sử dụng phương thức mới gọi Stored Procedure
    User existingUser = userDAO.getUserById(id);
    
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/edit.jsp");
    request.setAttribute("user", existingUser);
    dispatcher.forward(request, response);
}
```

2. **Cập nhật `insertUser`**: Thay thế `userDAO.insertUser(newUser)` bằng `userDAO.insertUserStore(newUser)`:
```java
private void insertUser(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, ServletException {
    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String country = request.getParameter("country");
    User newUser = new User(name, email, country);
    
    // Sử dụng phương thức mới gọi Stored Procedure
    userDAO.insertUserStore(newUser);
    // Áp dụng PRG pattern (sendRedirect) sau khi thêm mới thành công
    response.sendRedirect("users");
}
```

---

### Bước 5: Đóng gói và Triển khai lên Tomcat

Có 2 cách thực hiện:

#### Cách 1: Chạy file script tự động `deploy.bat`
Mở Terminal trong VS Code và chạy:
```bash
.\deploy.bat
```
Script sẽ tự động:
1. Chạy `mvn clean package` đóng gói thành `target/user-management.war`.
2. Copy `.war` vào thư mục `C:\Users\ADMIN\apache-tomcat-10.1.60\webapps\`.
3. Khởi động server Tomcat.

#### Cách 2: Sử dụng VS Code Task
- Nhấn tổ hợp phím **Ctrl + Shift + B** để chạy tác vụ **Maven: Build Package**.
- Vào menu **Terminal** -> **Run Task...** -> chọn **Deploy to Tomcat**.

---

## 3. Kiểm thử ứng dụng trên trình duyệt

Mở trình duyệt web và truy cập các đường dẫn:
1. **Trang chủ**: `http://localhost:8080/user-management/`
2. **Danh sách người dùng**: `http://localhost:8080/user-management/users`
3. **Thêm mới User**: Nhấn nút `+ Thêm Mới User` hoặc truy cập `http://localhost:8080/user-management/users?action=create`. Nhập thông tin và lưu lại (sẽ gọi thủ tục `insert_user`).
4. **Sửa User**: Nhấn nút `Edit` tại một dòng bất kỳ (sẽ gọi thủ tục `get_user_by_id` để lấy dữ liệu đổ vào form).
