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
    private static final String SELECT_ALL_USERS = "SELECT id, name, email, country FROM users ORDER BY id ASC;";
    private static final String DELETE_USERS_SQL = "DELETE FROM users WHERE id = ?;";
    private static final String UPDATE_USERS_SQL = "UPDATE users SET name = ?, email = ?, country = ? WHERE id = ?;";

    private static final String SP_GET_USER_BY_ID = "{CALL get_user_by_id(?)}";
    private static final String SP_INSERT_USER = "{CALL insert_user(?, ?, ?)}";
    private static final String SP_GET_ALL_USERS = "{CALL get_all_users()}";
    private static final String SP_UPDATE_USER = "{CALL update_user(?, ?, ?, ?)}";
    private static final String SP_DELETE_USER = "{CALL delete_user(?)}";

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

    protected Connection getConnection() {
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
    public void insertUserStore(User user) throws SQLException {
        try (Connection connection = getConnection();
                CallableStatement callableStatement = connection.prepareCall(SP_INSERT_USER)) {
            callableStatement.setString(1, user.getName());
            callableStatement.setString(2, user.getEmail());
            callableStatement.setString(3, user.getCountry());
            System.out.println("[CallableStatement] Executing: " + SP_INSERT_USER + " for " + user.getName());
            callableStatement.executeUpdate();
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
    public User getUserById(int id) {
        User user = null;
        try (Connection connection = getConnection();
                CallableStatement callableStatement = connection.prepareCall(SP_GET_USER_BY_ID)) {
            callableStatement.setInt(1, id);
            System.out.println("[CallableStatement] Executing: " + SP_GET_USER_BY_ID + " with ID=" + id);
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
    public List<User> selectAllUsers() {
        return selectAllUsersStore();
    }

    @Override
    public List<User> selectAllUsersStore() {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
                CallableStatement callableStatement = connection.prepareCall(SP_GET_ALL_USERS)) {
            System.out.println("[CallableStatement] Executing Stored Procedure: " + SP_GET_ALL_USERS);
            ResultSet rs = callableStatement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String country = rs.getString("country");
                users.add(new User(id, name, email, country));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        return updateUserStore(user);
    }

    @Override
    public boolean updateUserStore(User user) throws SQLException {
        boolean rowUpdated;
        try (Connection connection = getConnection();
                CallableStatement callableStatement = connection.prepareCall(SP_UPDATE_USER)) {
            callableStatement.setInt(1, user.getId());
            callableStatement.setString(2, user.getName());
            callableStatement.setString(3, user.getEmail());
            callableStatement.setString(4, user.getCountry());
            System.out.println(
                    "[CallableStatement] Executing Stored Procedure: " + SP_UPDATE_USER + " for ID=" + user.getId());
            rowUpdated = callableStatement.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    @Override
    public boolean deleteUser(int id) throws SQLException {
        return deleteUserStore(id);
    }

    @Override
    public boolean deleteUserStore(int id) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = getConnection();
                CallableStatement callableStatement = connection.prepareCall(SP_DELETE_USER)) {
            callableStatement.setInt(1, id);
            System.out.println("[CallableStatement] Executing Stored Procedure: " + SP_DELETE_USER + " for ID=" + id);
            rowDeleted = callableStatement.executeUpdate() > 0;
        }
        return rowDeleted;
    }
}
