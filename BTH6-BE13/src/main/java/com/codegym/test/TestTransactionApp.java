package com.codegym.test;

import com.codegym.dao.UserDAO;
import com.codegym.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestTransactionApp {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        System.out.println("*************************************************************");
        System.out.println("*   KIỂM THỬ [BÀI TẬP] LUYỆN TẬP MYSQL JDBC TRANSACTION     *");
        System.out.println("*************************************************************");

        System.out.println("\n-------------------------------------------------------------");
        System.out.println("[KỊCH BẢN 1] Cố tình gây lỗi SQL khi chèn quyền hạn");
        System.out.println("Kỳ vọng: Ngoại lệ SQLException xuất hiện -> Rollback -> User KHÔNG tồn tại trong DB.");
        System.out.println("-------------------------------------------------------------");

        String errorUserName = "User_Simulate_Error_" + (System.currentTimeMillis() % 10000);
        User user1 = new User(errorUserName, "error.test@codegym.vn", "Vietnam");
        int[] permissions1 = { 1, 2 };
        try {
            userDAO.addUserTransaction(user1, permissions1, true);
            System.out.println("=> Kết quả bất thường: Không có lỗi xảy ra!");
        } catch (SQLException e) {
            System.out.println("=> ĐÃ BẮT ĐƯỢC LỖI KỲ VỌNG: " + e.getMessage());
        }

        boolean user1Exists = checkUserExists(userDAO, errorUserName);
        System.out.println(">>> KIỂM TRA MYSQL CHO USER 1 ('" + errorUserName + "'): "
                + (user1Exists ? "❌ THẤT BẠI (User vẫn còn trong DB)"
                        : "✅ THÀNH CÔNG (User đã bị ROLLBACK hoàn toàn!)"));

        System.out.println("\n-------------------------------------------------------------");
        System.out.println("[KỊCH BẢN 2] Chạy câu lệnh SQL chuẩn xác");
        System.out.println("Kỳ vọng: Thực thi trơn tru -> Commit() -> User và Quyền hạn ĐƯỢC LƯU trong DB.");
        System.out.println("-------------------------------------------------------------");

        String successUserName = "User_Commit_Ok_" + (System.currentTimeMillis() % 10000);
        User user2 = new User(successUserName, "success.test@codegym.vn", "Vietnam");
        int[] permissions2 = { 1, 4 }; // Quyền: Add, View

        try {
            userDAO.addUserTransaction(user2, permissions2, false);
            System.out.println("=> Giao dịch đã hoàn tất không có lỗi!");
        } catch (SQLException e) {
            System.out.println("=> Lỗi ngoài ý muốn: " + e.getMessage());
        }

        boolean user2Exists = checkUserExists(userDAO, successUserName);
        System.out.println(">>> KIỂM TRA MYSQL CHO USER 2 ('" + successUserName + "'): "
                + (user2Exists ? "✅ THÀNH CÔNG (User đã được COMMIT vĩnh viễn vào DB!)"
                        : "❌ THẤT BẠI (Không tìm thấy User)"));

        System.out.println("\n*************************************************************");
        System.out.println("*                 KẾT THÚC QUÁ TRÌNH TEST                   *");
        System.out.println("*************************************************************\n");
    }

    private static boolean checkUserExists(UserDAO dao, String name) {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ?";
        try (Connection conn = dao.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
