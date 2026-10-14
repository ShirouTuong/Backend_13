package com.codegym.controller;

import com.codegym.dao.IUserDAO;
import com.codegym.dao.UserDAO;
import com.codegym.model.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = { "/users" })
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IUserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "create":
                    showNewForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteUser(request, response);
                    break;
                case "test-rollback":
                    testTransactionRollback(request, response);
                    break;
                case "test-commit":
                    testTransactionCommit(request, response);
                    break;
                default:
                    listUser(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "create":
                    insertUser(request, response);
                    break;
                case "edit":
                    updateUser(request, response);
                    break;
                default:
                    listUser(request, response);
                    break;
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void listUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {
        List<User> listUser = userDAO.selectAllUsers();
        request.setAttribute("listUser", listUser);

        var session = request.getSession(false);
        if (session != null) {
            String message = (String) session.getAttribute("message");
            String messageType = (String) session.getAttribute("messageType");
            String messageDetail = (String) session.getAttribute("messageDetail");
            if (message != null) {
                request.setAttribute("message", message);
                request.setAttribute("messageType", messageType);
                request.setAttribute("messageDetail", messageDetail);
                session.removeAttribute("message");
                session.removeAttribute("messageType");
                session.removeAttribute("messageDetail");
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("user/list.jsp");
        dispatcher.forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("user/create.jsp");
        dispatcher.forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        User existingUser = userDAO.getUserById(id);

        RequestDispatcher dispatcher = request.getRequestDispatcher("user/edit.jsp");
        request.setAttribute("user", existingUser);
        dispatcher.forward(request, response);
    }

    private void insertUser(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String country = request.getParameter("country");

        String[] permissionsStr = request.getParameterValues("permissions");
        int[] permissions = null;

        if (permissionsStr != null) {
            permissions = new int[permissionsStr.length];
            for (int i = 0; i < permissionsStr.length; i++) {
                permissions[i] = Integer.parseInt(permissionsStr[i]);
            }
        }

        String simulateErrorParam = request.getParameter("simulateError");
        boolean simulateError = "true".equalsIgnoreCase(simulateErrorParam)
                || "on".equalsIgnoreCase(simulateErrorParam);

        User newUser = new User(name, email, country);

        var session = request.getSession();
        try {
            userDAO.addUserTransaction(newUser, permissions, simulateError);
            session.setAttribute("message", "Thêm người dùng thành công (Transaction đã COMMIT)!");
            session.setAttribute("messageType", "success");
            session.setAttribute("messageDetail",
                    "User '" + name + "' cùng các quyền hạn đã được lưu vĩnh viễn vào CSDL.");
        } catch (SQLException e) {
            session.setAttribute("message", "⚠️ Transaction đã ROLLBACK do phát hiện lỗi SQL!");
            session.setAttribute("messageType", "danger");
            session.setAttribute("messageDetail", "Chi tiết lỗi: " + e.getMessage() + ". Dữ liệu của user '" + name
                    + "' KHÔNG được lưu vào bảng users (bảo toàn tính Atomicity).");
        }

        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void testTransactionRollback(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        var session = request.getSession();
        String testName = "User_Test_Loi_Rollback_" + (System.currentTimeMillis() % 1000);
        User testUser = new User(testName, "test.error@codegym.vn", "Vietnam");
        int[] permissions = { 1, 2, 4 };

        try {
            userDAO.addUserTransaction(testUser, permissions, true);
            session.setAttribute("message", "Thành công (Bất ngờ)!");
            session.setAttribute("messageType", "success");
        } catch (SQLException e) {
            session.setAttribute("message",
                    "⚡ [TEST ROLLBACK THÀNH CÔNG] Ngoại lệ SQL đã được kích hoạt và Transaction đã ROLLBACK!");
            session.setAttribute("messageType", "danger");
            session.setAttribute("messageDetail", "Lỗi SQL giả lập: " + e.getMessage() + " => Thao tác INSERT User '"
                    + testName + "' đã bị hủy bỏ hoàn toàn! Kiểm tra CSDL sẽ thấy KHÔNG có user này.");
        }
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void testTransactionCommit(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        var session = request.getSession();
        String testName = "User_Test_Commit_" + (System.currentTimeMillis() % 1000);
        User testUser = new User(testName, "test.commit@codegym.vn", "Vietnam");
        int[] permissions = { 1, 4 };

        try {
            userDAO.addUserTransaction(testUser, permissions, false);
            session.setAttribute("message", "✅ [TEST COMMIT THÀNH CÔNG] Transaction đã COMMIT hoàn tất!");
            session.setAttribute("messageType", "success");
            session.setAttribute("messageDetail",
                    "User '" + testName + "' và 2 quyền (add, view) đã được ghi nhận vĩnh viễn vào CSDL.");
        } catch (SQLException e) {
            session.setAttribute("message", "Lỗi: " + e.getMessage());
            session.setAttribute("messageType", "danger");
        }
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String country = request.getParameter("country");

        User book = new User(id, name, email, country);
        userDAO.updateUser(book);
        response.sendRedirect("users");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {
        int id = Integer.parseInt(request.getParameter("id"));
        userDAO.deleteUser(id);
        response.sendRedirect("users");
    }
}
