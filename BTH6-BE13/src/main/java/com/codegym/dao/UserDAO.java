package com.codegym.dao;

import com.codegym.model.User;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {
    private String jdbcURL = "jdbc:mysql://localhost:3306/demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private String jdbcUsername = "root";
    private String jdbcPassword = "Matkhaumoi1@";

    private static final String INSERT_USERS_SQL = "INSERT INTO users (name, email, country) VALUES (?, ?, ?);";
    private static final String SELECT_USER_BY_ID = "SELECT id, name, email, country FROM users WHERE id = ?;";
    private static final String SELECT_ALL_USERS = "SELECT id, name, email, country FROM users;";
    private static final String DELETE_USERS_SQL = "DELETE FROM users WHERE id = ?;";
    private static final String UPDATE_USERS_SQL = "UPDATE users SET name = ?, email = ?, country = ? WHERE id = ?;";

    public UserDAO() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            this.jdbcURL = envUrl.trim();
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) {
            this.jdbcUsername = envUser.trim();
        }
        String envPassword = System.getenv("DB_PASSWORD");
        if (envPassword != null) {
            this.jdbcPassword = envPassword.trim();
        }
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public void insertUser(User user) throws SQLException {
        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getCountry());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public User selectUser(int id) {
        User user = null;
        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID)) {
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
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

    @Override
    public List<User> selectAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String country = rs.getString("country");
                User user = new User(id, name, email, country);
                user.setPermissions(getUserPermissions(id));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean deleteUser(int id) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_USERS_SQL)) {
            statement.setInt(1, id);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        boolean rowUpdated;
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_USERS_SQL)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountry());
            statement.setInt(4, user.getId());
            rowUpdated = statement.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    @Override
    public User getUserById(int id) {
        User user = null;
        String query = "{CALL get_user_by_id(?)}";

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

    @Override
    public void insertUserStore(User user) throws SQLException {
        String query = "{CALL insert_user(?, ?, ?)}";

        try (Connection connection = getConnection();
                CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setString(1, user.getName());
            callableStatement.setString(2, user.getEmail());
            callableStatement.setString(3, user.getCountry());
            System.out.println(callableStatement);
            callableStatement.executeUpdate();
        }
    }

    @Override
    public void addUserTransaction(User user, int[] permissionIds) throws SQLException {
        addUserTransaction(user, permissionIds, false);
    }

    /**
     * [BÀI TẬP] Luyện tập MySQL JDBC Transaction:
     * Sử dụng commit() và rollback() của đối tượng Connection.
     * Sửa đổi mã nguồn để gây ra lỗi SQL và quan sát kết quả trong CSDL.
     *
     * @param user             Đối tượng User cần thêm vào bảng users
     * @param permissionIds    Danh sách ID quyền gán cho user trong user_permission
     * @param simulateSqlError Nếu true: cố tình thực thi câu lệnh SQL lỗi để kích
     *                         hoạt rollback()
     */
    @Override
    public void addUserTransaction(User user, int[] permissionIds, boolean simulateSqlError) throws SQLException {
        Connection connection = null;
        PreparedStatement pstmtUser = null;
        PreparedStatement pstmtAssignment = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            System.out.println("\n=======================================================");
            System.out.println("[JDBC TRANSACTION] Bắt đầu Transaction mới (autoCommit = false)...");

            String insertUsersSql = "INSERT INTO users (name, email, country) VALUES(?, ?, ?)";
            pstmtUser = connection.prepareStatement(insertUsersSql, java.sql.Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, user.getName());
            pstmtUser.setString(2, user.getEmail());
            pstmtUser.setString(3, user.getCountry());
            int affected = pstmtUser.executeUpdate();

            rs = pstmtUser.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }
            System.out.println("[JDBC TRANSACTION] Bước 1: Đã INSERT User tạm thời (id=" + userId + ", name="
                    + user.getName() + "). Dòng ảnh hưởng: " + affected);
            System.out.println(
                    "[JDBC TRANSACTION] >>> Lưu ý: Dữ liệu này CHƯA được commit vĩnh viễn vào MySQL, đang nằm trong Transaction!");

            if (permissionIds != null && permissionIds.length > 0) {
                String insertPermissionSql;
                if (simulateSqlError) {
                    System.out.println("[JDBC TRANSACTION] ⚠️ [MÔ PHỎNG LỖI SQL THEO ĐỀ BÀI]");
                    System.out.println(
                            "[JDBC TRANSACTION] ⚠️ Đang thực thi câu lệnh SQL cố tình lỗi vào user_permission...");
                    insertPermissionSql = "INSERT INTO user_permission (user_id, permission_id, cot_khong_ton_tai_gay_loi) VALUES(?, ?, ?)";
                } else {
                    insertPermissionSql = "INSERT INTO user_permission (user_id, permission_id) VALUES(?, ?)";
                }

                pstmtAssignment = connection.prepareStatement(insertPermissionSql);

                for (int permissionId : permissionIds) {
                    pstmtAssignment.setInt(1, userId);
                    pstmtAssignment.setInt(2, permissionId);
                    pstmtAssignment.executeUpdate();
                }
                System.out.println("[JDBC TRANSACTION] Bước 2: Đã INSERT " + permissionIds.length
                        + " quyền vào user_permission thành công.");
            }

            connection.commit();
            System.out
                    .println(">>> [SUCCESS] Transaction đã được COMMIT thành công! Dữ liệu đã lưu vĩnh viễn vào CSDL.");
            System.out.println("=======================================================\n");

        } catch (SQLException e) {
            System.err.println("\n>>> [ERROR] Phát hiện ngoại lệ SQLException trong Transaction: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback();
                    System.err.println(">>> [ROLLBACK] connection.rollback() đã được thực thi thành công!");
                    System.err.println(
                            ">>> [ROLLBACK KẾT QUẢ] Thao tác INSERT User ở Bước 1 đã bị HỦY BỎ hoàn toàn! Bảng users KHÔNG có dữ liệu rác!");
                    System.err.println("=======================================================\n");
                }
            } catch (SQLException ex) {
                System.err.println(">>> [ERROR] Lỗi khi thực hiện rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (pstmtUser != null) {
                try {
                    pstmtUser.close();
                } catch (SQLException ignored) {
                }
            }
            if (pstmtAssignment != null) {
                try {
                    pstmtAssignment.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public List<String> getUserPermissions(int userId) {
        List<String> permissions = new ArrayList<>();
        String sql = "SELECT p.name FROM permission p JOIN user_permission up ON p.id = up.permission_id WHERE up.user_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }
}
