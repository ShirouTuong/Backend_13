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
                case "test-without-tran":
                    testWithoutTran(request, response);
                    break;
                default:
                    listUser(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void testWithoutTran(HttpServletRequest request, HttpServletResponse response) {
        userDAO.insertUpdateWithoutTransaction();
        System.out.println("Đã chạy xong hàm testWithoutTran. Hãy kiểm tra database...");
        try {
            response.getWriter().println(
                    "<html><head><meta charset='UTF-8'><title>Ket qua Test Without Transaction</title></head>");
            response.getWriter()
                    .println("<body style='font-family: Arial, sans-serif; padding: 30px; line-height: 1.6;'>");
            response.getWriter()
                    .println("<h2 style='color: #d9534f;'>[Thực hành] Thực thi SQL không sử dụng Transaction</h2>");
            response.getWriter().println(
                    "<div style='background: #f8f9fa; border-left: 4px solid #f0ad4e; padding: 15px; margin: 15px 0;'>");
            response.getWriter().println(
                    "<p><b>1. Kiểm tra Log Console:</b> Đã bắt được ngoại lệ <code>SQLException: No value specified for parameter 1</code> do tham số thứ 1 ở câu lệnh UPDATE bị thiếu giá trị.</p>");
            response.getWriter().println("<p><b>2. Kiểm tra MySQL Database (bảng Employee):</b>");
            response.getWriter().println(
                    "<br>&bull; 2 dòng (Quynh: 10, Ngan: 20) <b>vẫn được lưu vĩnh viễn</b> do auto-commit mặc định.");
            response.getWriter()
                    .println("<br>&bull; Lương của Quynh <b>không được cập nhật lên 999.99</b> do gặp lỗi.</p>");
            response.getWriter().println(
                    "<p style='color: red;'><b>&rArr; Kết luận:</b> Hiện tượng rác dữ liệu (dữ liệu được chèn một nửa) xảy ra vì không sử dụng Transaction (thiếu nguyên lý All-or-Nothing).</p>");
            response.getWriter().println("</div>");
            response.getWriter().println(
                    "<a href='users' style='display:inline-block; padding: 10px 20px; background:#007bff; color:white; text-decoration:none; border-radius:4px;'>&larr; Về danh sách User</a>");
            response.getWriter().println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
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

        User newUser = new User(name, email, country);

        userDAO.addUserTransaction(newUser, permissions);

        response.sendRedirect("users");
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
