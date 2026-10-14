# [Thực hành] Thực thi SQL không sử dụng Transaction

Dự án thực hành tìm hiểu bản chất của **JDBC Transaction** và nguyên lý **ACID (Atomicity - All or Nothing)** trong cơ sở dữ liệu MySQL, được xây dựng và kiểm thử bằng **Visual Studio Code**, **Tomcat 10**, và **MySQL Workbench**.

---

## 1. Mục Tiêu & Bản Chất Vấn Đề

- **Mục tiêu:** Quan sát trực tiếp hiện tượng **"rác dữ liệu" (dữ liệu được chèn một nửa / nửa vời)** khi một chuỗi thao tác nghiệp vụ gặp sự cố nhưng không được bao bọc trong một Transaction.
- **Bản chất kỹ thuật:** 
  - Mặc định, kết nối JDBC hoạt động ở chế độ **Auto-Commit (`autoCommit = true`)**. Mỗi khi một câu lệnh SQL như `INSERT`, `UPDATE` hay `DELETE` được thực thi qua phương thức `execute()`, JDBC sẽ lập tức commit vĩnh viễn thay đổi đó vào cơ sở dữ liệu.
  - Khi một chuỗi gồm nhiều câu lệnh nghiệp vụ (ví dụ: Chèn nhân viên 1, Chèn nhân viên 2, Cập nhật lương nhân viên 1): nếu câu lệnh thứ 3 bị lỗi (do cú pháp, thiếu tham số, khóa ngoại...), thì 2 câu lệnh trước đó **vẫn tồn tại vĩnh viễn trong CSDL**, dẫn tới trạng thái dữ liệu dở dang, không toàn vẹn.
  - Trong thực tế (như ứng dụng ngân hàng: trừ tiền tài khoản A nhưng chưa cộng tiền vào tài khoản B), việc không có Transaction là một lỗi nghiêm trọng.

---

## 2. Cấu Trúc Dự Án trong VS Code

```text
BTH3-BE13/
├── .vscode/
│   └── tasks.json                     # Các tác vụ tự động hóa trong VS Code (Build, Deploy, MySQL scripts)
├── database/
│   ├── setup_employee.sql             # Script tạo và kiểm tra bảng Employee
│   ├── setup_demo_db.sql              # Script khởi tạo CSDL demo gốc
│   └── setup_transaction_db.sql       # Script phân quyền từ bài thực hành trước
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── codegym/
│       │           ├── controller/
│       │           │   └── UserServlet.java    # Bổ sung routing case "test-without-tran"
│       │           ├── dao/
│       │           │   ├── IUserDAO.java       # Khai báo phương thức insertUpdateWithoutTransaction()
│       │           │   └── UserDAO.java        # Triển khai SQL và cố tình tạo lỗi tham số Update
│       │           └── model/
│       │               └── User.java           # Model User
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml                     # Servlet mapping
│           ├── user/
│           │   ├── list.jsp                    # Danh sách người dùng + Nút Test Without Transaction
│           │   ├── create.jsp                  # Thêm người dùng
│           │   └── edit.jsp                    # Sửa người dùng
│           └── index.jsp                       # Trang chủ điều hướng bài lab
├── deploy.bat                                 # Script tự động đóng gói Maven và triển khai lên Tomcat
└── pom.xml                                    # Cấu hình dự án Maven (Jakarta EE 10, MySQL Connector/J)
```

---

## 3. Hướng Dẫn Thực Hiện Từng Bước

### Bước 1: Thao tác trên MySQL Workbench (Cập nhật CSDL)

1. Khởi động **MySQL Workbench**.
2. Kết nối tới MySQL Server cục bộ (user `root`, mật khẩu `Matkhaumoi1@`).
3. Mở tab truy vấn mới (**Ctrl + T**) và thực thi script sau (hoặc mở file `database/setup_employee.sql`):

```sql
USE demo;

CREATE TABLE IF NOT EXISTS Employee (
    id INT(3) NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    salary INT(220) NOT NULL,
    created_Date DATETIME,
    PRIMARY KEY (id)
);
```

4. Bấm biểu tượng tia sét ⚡ (**Execute**) để tạo bảng.

---

### Bước 2: Cập nhật Interface `IUserDAO.java` trong VS Code

Mở file [`src/main/java/com/codegym/dao/IUserDAO.java`](src/main/java/com/codegym/dao/IUserDAO.java) và bổ sung thêm chữ ký phương thức:

```java
public void insertUpdateWithoutTransaction();
```

---

### Bước 3: Triển khai Logic trong `UserDAO.java` (Cố tình tạo lỗi)

Mở file [`src/main/java/com/codegym/dao/UserDAO.java`](src/main/java/com/codegym/dao/UserDAO.java):

1. **Khai báo các thư viện cần thiết:**
   ```java
   import java.math.BigDecimal;
   import java.sql.Statement;
   import java.sql.Timestamp;
   import java.time.LocalDateTime;
   ```

2. **Bổ sung các hằng số câu lệnh SQL:**
   ```java
   private static final String SQL_INSERT = "INSERT INTO Employee (name, salary, created_date) VALUES (?,?,?)";
   private static final String SQL_UPDATE = "UPDATE Employee SET salary=? WHERE name=?";
   private static final String SQL_TABLE_CREATE = "CREATE TABLE Employee"
           + "("
           + " id INT(11) AUTO_INCREMENT,"
           + " name VARCHAR(100) NOT NULL,"
           + " salary DECIMAL(15, 2) NOT NULL,"
           + " created_date TIMESTAMP,"
           + " PRIMARY KEY (id)"
           + ")";
   private static final String SQL_TABLE_DROP = "DROP TABLE IF EXISTS Employee";
   ```

3. **Triển khai phương thức `insertUpdateWithoutTransaction()`:**
   > **Lưu ý quan trọng:** Cố tình cấu hình sai tham số ở câu lệnh Update (`psUpdate.setBigDecimal(2, ...)` thay vì gán tham số 1) để ép JDBC quăng ra ngoại lệ `SQLException: No value specified for parameter 1`.

   ```java
   @Override
   public void insertUpdateWithoutTransaction() {
       try (Connection conn = getConnection();
               Statement statement = conn.createStatement();
               PreparedStatement psInsert = conn.prepareStatement(SQL_INSERT);
               PreparedStatement psUpdate = conn.prepareStatement(SQL_UPDATE)) {

           // 1. Xóa bảng cũ và tạo lại bảng mới để làm sạch dữ liệu mỗi lần test
           statement.execute(SQL_TABLE_DROP);
           statement.execute(SQL_TABLE_CREATE);

           // 2. Chạy tập lệnh Insert (chèn 2 nhân viên)
           psInsert.setString(1, "Quynh");
           psInsert.setBigDecimal(2, new BigDecimal(10));
           psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
           psInsert.execute();

           psInsert.setString(1, "Ngan");
           psInsert.setBigDecimal(2, new BigDecimal(20));
           psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
           psInsert.execute();

           // 3. Chạy tập lệnh Update (cố tình tạo lỗi)
           // Dòng bên dưới sẽ gây lỗi vì tham số index 1 chưa được gán giá trị:
           psUpdate.setBigDecimal(2, new BigDecimal(999.99));
           // Lệnh đúng ra phải là: psUpdate.setBigDecimal(1, new BigDecimal(999.99));
           //                     psUpdate.setString(2, "Quynh");

           // Lệnh execute này sẽ ném ra Exception
           psUpdate.execute();

       } catch (Exception e) {
           System.out.println("Đã bắt được lỗi trong quá trình thực thi SQL:");
           e.printStackTrace();
       }
   }
   ```

---

### Bước 4: Bổ sung Routing trong `UserServlet.java`

Mở file [`src/main/java/com/codegym/controller/UserServlet.java`](src/main/java/com/codegym/controller/UserServlet.java):

1. Trong phương thức `doGet()`, thêm `case` vào khối `switch (action)`:
   ```java
   case "test-without-tran":
       testWithoutTran(request, response);
       break;
   ```

2. Tạo phương thức `testWithoutTran()` ngay dưới `doGet()`:
   ```java
   private void testWithoutTran(HttpServletRequest request, HttpServletResponse response) {
       // Gọi phương thức cố tình sinh lỗi
       userDAO.insertUpdateWithoutTransaction();
       System.out.println("Đã chạy xong hàm testWithoutTran. Hãy kiểm tra database...");
   }
   ```

---

## 4. Biên Dịch, Triển Khai & Kiểm Nghiệm Thực Tế

### Cách 1: Chạy file `deploy.bat` hoặc Task trong VS Code
- Chạy file `deploy.bat` trong thư mục gốc dự án. Script sẽ tự động:
  1. Biên dịch dự án bằng Maven (`mvn clean package`).
  2. Sao chép `target/user-management.war` sang thư mục `webapps` của Tomcat.
  3. Khởi động Tomcat Server.

### Cách 2: Kích hoạt kiểm thử
1. Mở trình duyệt web và truy cập vào đường dẫn:
   ```text
   http://localhost:8080/user-management/users?action=test-without-tran
   ```
   *(Hoặc click vào nút đỏ **"⚠ Test Without Transaction"** trên thanh công cụ trang danh sách User)*.

2. **Kiểm tra Log Console trong VS Code / Tomcat:**
   Bạn sẽ thấy thông báo bắt lỗi và một stack trace màu đỏ chỉ ra chính xác nguyên nhân:
   ```text
   Đã bắt được lỗi trong quá trình thực thi SQL:
   java.sql.SQLException: No value specified for parameter 1
       at com.mysql.cj.jdbc.ClientPreparedStatement.execute(...)
       at com.codegym.dao.UserDAO.insertUpdateWithoutTransaction(UserDAO.java:298)
   Đã chạy xong hàm testWithoutTran. Hãy kiểm tra database...
   ```

3. **Kiểm tra hệ lụy trong MySQL Workbench:**
   Mở MySQL Workbench và thực thi câu truy vấn:
   ```sql
   USE demo;
   SELECT * FROM Employee;
   ```

   **Kết quả hiển thị trên bảng dữ liệu:**
   | id | name | salary | created_date |
   |:---|:---|:---|:---|
   | **1** | **Quynh** | **10.00** *(chưa được cập nhật lên 999.99)* | 2026-10-14 22:06:46 |
   | **2** | **Ngan** | **20.00** | 2026-10-14 22:06:46 |

---

## 5. Kết Luận Cốt Lõi

1. **Hiện tượng:** Dù quá trình thực thi nghiệp vụ bị lỗi ở câu lệnh thứ 3 (cập nhật lương), 2 câu lệnh chèn dữ liệu nhân viên `Quynh` và `Ngan` vẫn **được lưu vĩnh viễn** vào cơ sở dữ liệu.
2. **Nguyên nhân:** Do chế độ mặc định của JDBC là **auto-commit = true**. Mỗi câu lệnh độc lập được gửi xuống database và commit ngay khi hoàn tất.
3. **Khắc phục:** Để đảm bảo tính toàn vẹn (tính nguyên tử Atomicity), ta cần:
   - Tắt auto-commit: `connection.setAutoCommit(false);`
   - Gọi `connection.commit();` khi toàn bộ chuỗi lệnh thành công.
   - Gọi `connection.rollback();` trong khối `catch` để hủy bỏ toàn bộ các lệnh trước đó nếu có bất kỳ lỗi nào xảy ra.
