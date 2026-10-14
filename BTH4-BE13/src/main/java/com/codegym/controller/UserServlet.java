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
import java.io.PrintWriter;
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
                // Bước 3: Bổ sung case "test-use-tran"
                case "test-use-tran":
                    testUseTran(request, response);
                    break;
                default:
                    listUser(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Bước 3: Phương thức testUseTran() gọi hàm xử lý từ DAO
     */
    private void testUseTran(HttpServletRequest request, HttpServletResponse response) {
        String fixParam = request.getParameter("fix");
        boolean isFixed = "true".equalsIgnoreCase(fixParam);

        userDAO.insertUpdateUseTransaction(isFixed);
        System.out.println("Hoàn tất gọi hàm testUseTran! (Chế độ sửa lỗi: " + isFixed + ")");

        try {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='vi'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Kết Quả Thực Thi: JDBC Transaction</title>");
            out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
            out.println("<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>");
            out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
            out.println("<style>");
            out.println(":root { --primary: #4f46e5; --bg: #0f172a; --card: rgba(30, 41, 59, 0.85); --text: #f8fafc; --muted: #94a3b8; --border: rgba(148, 163, 184, 0.15); --success: #10b981; --warning: #f59e0b; --danger: #ef4444; }");
            out.println("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', sans-serif; }");
            out.println("body { min-height: 100vh; background: radial-gradient(circle at top right, #1e1b4b, #0f172a 60%); color: var(--text); padding: 40px 20px; display: flex; justify-content: center; align-items: center; }");
            out.println(".container { max-width: 800px; width: 100%; background: var(--card); backdrop-filter: blur(16px); border: 1px solid var(--border); border-radius: 24px; padding: 40px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); }");
            out.println(".badge { display: inline-block; padding: 5px 14px; background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(52, 211, 153, 0.3); border-radius: 9999px; font-size: 0.82rem; font-weight: 700; text-transform: uppercase; margin-bottom: 16px; }");
            out.println("h2 { font-size: 1.8rem; font-weight: 800; margin-bottom: 20px; color: #ffffff; }");
            out.println(".status-card { border-radius: 16px; padding: 20px; margin-bottom: 24px; font-size: 0.95rem; line-height: 1.65; }");
            out.println(".status-err { background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.35); }");
            out.println(".status-ok { background: rgba(16, 185, 129, 0.12); border: 1px solid rgba(16, 185, 129, 0.35); }");
            out.println(".status-title { font-size: 1.15rem; font-weight: 700; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }");
            out.println(".box { background: rgba(15, 23, 42, 0.6); border: 1px solid var(--border); border-radius: 16px; padding: 20px; margin-bottom: 24px; font-size: 0.95rem; line-height: 1.65; }");
            out.println(".box h3 { font-size: 1.05rem; margin-bottom: 10px; color: #38bdf8; }");
            out.println("code { background: rgba(255,255,255,0.1); padding: 2px 6px; border-radius: 4px; font-family: Consolas, monospace; color: #fbbf24; font-size: 0.9rem; }");
            out.println(".actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 28px; }");
            out.println(".btn { display: inline-flex; align-items: center; justify-content: center; padding: 12px 20px; border-radius: 12px; font-size: 0.95rem; font-weight: 600; text-decoration: none; transition: 0.2s; cursor: pointer; border: none; }");
            out.println(".btn-primary { background: var(--primary); color: white; }");
            out.println(".btn-primary:hover { background: #4338ca; transform: translateY(-1px); }");
            out.println(".btn-ghost { background: rgba(255,255,255,0.06); color: var(--text); border: 1px solid var(--border); }");
            out.println(".btn-ghost:hover { background: rgba(255,255,255,0.12); }");
            out.println(".btn-run1 { background: rgba(239, 68, 68, 0.25); color: #fca5a5; border: 1px solid rgba(239, 68, 68, 0.4); }");
            out.println(".btn-run1:hover { background: rgba(239, 68, 68, 0.35); }");
            out.println(".btn-run2 { background: rgba(16, 185, 129, 0.25); color: #6ee7b7; border: 1px solid rgba(16, 185, 129, 0.4); }");
            out.println(".btn-run2:hover { background: rgba(16, 185, 129, 0.35); }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='container'>");
            out.println("<span class='badge'>BTH4: JDBC Transaction</span>");
            out.println("<h2>Thực thi SQL có sử dụng Transaction</h2>");

            if (!isFixed) {
                out.println("<div class='status-card status-err'>");
                out.println("<div class='status-title' style='color: #f87171;'>&#9888; Lần 1: Chạy với mã cố tình gây lỗi (Index = 2)</div>");
                out.println("<p>Lệnh Update cố tình đặt sai vị trí tham số: <code>psUpdate.setBigDecimal(2, ...)</code> gây ra ngoại lệ <code>SQLException: Parameter index out of range</code>.</p>");
                out.println("<p style='margin-top: 6px; font-weight: 600; color: #fca5a5;'>&rArr; Kết quả thực tế: Luồng code bị ngắt, lệnh <code>conn.commit()</code> KHÔNG được gọi &rarr; Tự động ROLLBACK toàn bộ các lệnh Insert trước đó!</p>");
                out.println("</div>");
            } else {
                out.println("<div class='status-card status-ok'>");
                out.println("<div class='status-title' style='color: #34d399;'>&#10004; Lần 2: Sửa lỗi để ứng dụng hoạt động chuẩn xác (Index = 1)</div>");
                out.println("<p>Lệnh Update đã được sửa đúng vị trí tham số: <code>psUpdate.setBigDecimal(1, new BigDecimal(999.99))</code>.</p>");
                out.println("<p style='margin-top: 6px; font-weight: 600; color: #6ee7b7;'>&rArr; Kết quả thực tế: Toàn bộ các thao tác thành công trọn vẹn và hội tụ tại <code>conn.commit()</code> &rarr; Dữ liệu được ghi nhận vĩnh viễn!</p>");
                out.println("</div>");
            }

            out.println("<div class='box'>");
            out.println("<h3>Kiểm tra kết quả trong MySQL Workbench:</h3>");
            out.println("<p>Mở MySQL Workbench và thực hiện câu lệnh truy vấn:</p>");
            out.println("<p style='margin: 8px 0;'><code>USE demo; SELECT * FROM Employee;</code></p>");
            if (!isFixed) {
                out.println("<p>&bull; <b>Kết quả mong đợi:</b> <b>Bảng trống trơn (0 bản ghi)</b>. Trạng thái ban đầu được khôi phục hoàn hảo, không có rác dữ liệu!</p>");
            } else {
                out.println("<p>&bull; <b>Kết quả mong đợi:</b> <b>Có 2 bản ghi</b> (Quynh lương 999.99, Ngan lương 20). Dữ liệu nhất quán hoàn toàn!</p>");
            }
            out.println("</div>");

            out.println("<div class='actions'>");
            if (!isFixed) {
                out.println("<a href='users?action=test-use-tran&fix=true' class='btn btn-run2'>&#9654; Chạy Thử Lần 2: Sửa Đúng (Commit)</a>");
            } else {
                out.println("<a href='users?action=test-use-tran' class='btn btn-run1'>&#9654; Chạy Thử Lần 1: Cố Tình Lỗi (Rollback)</a>");
            }
            out.println("<a href='users' class='btn btn-primary'>&larr; Quay lại Danh sách User</a>");
            out.println("<a href='users?action=test-without-tran' class='btn btn-ghost'>So sánh Không Transaction (BTH3)</a>");
            out.println("</div>");

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testWithoutTran(HttpServletRequest request, HttpServletResponse response) {
        userDAO.insertUpdateWithoutTransaction();
        System.out.println("Đã chạy xong hàm testWithoutTran. Hãy kiểm tra database...");
        try {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='vi'>");
            out.println("<head><meta charset='UTF-8'><title>Kết quả Test Không Dùng Transaction</title>");
            out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>");
            out.println("<style>");
            out.println("body { font-family: 'Inter', sans-serif; background: #0f172a; color: #f8fafc; padding: 40px 20px; display: flex; justify-content: center; }");
            out.println(".card { background: rgba(30, 41, 59, 0.8); border: 1px solid rgba(148, 163, 184, 0.15); border-radius: 20px; padding: 32px; max-width: 720px; }");
            out.println("h2 { color: #f87171; margin-bottom: 16px; }");
            out.println(".note { background: rgba(239, 68, 68, 0.1); border-left: 4px solid #ef4444; padding: 14px; margin: 16px 0; border-radius: 6px; font-size: 0.95rem; line-height: 1.6; }");
            out.println(".btn { display: inline-block; padding: 10px 20px; background: #4f46e5; color: white; text-decoration: none; border-radius: 10px; font-weight: 600; margin-top: 14px; }");
            out.println("</style></head>");
            out.println("<body><div class='card'>");
            out.println("<h2>[Thực hành] Thực thi SQL KHÔNG sử dụng Transaction (BTH3)</h2>");
            out.println("<div class='note'>");
            out.println("<p><b>1. Kiểm tra Console Log:</b> Đã bắt được ngoại lệ <code>SQLException: No value specified for parameter 1</code>.</p>");
            out.println("<p><b>2. Kiểm tra MySQL Database (bảng Employee):</b></p>");
            out.println("<p>&bull; 2 dòng (Quynh: 10, Ngan: 20) <b>vẫn bị lưu vĩnh viễn</b> do auto-commit mặc định.</p>");
            out.println("<p>&bull; Lương của Quynh <b>không được cập nhật lên 999.99</b> do gặp lỗi.</p>");
            out.println("<p style='color: #fca5a5; margin-top: 8px;'><b>&rArr; Kết luận:</b> Xảy ra hiện tượng rác dữ liệu vì không có Transaction (vi phạm nguyên lý All-or-Nothing).</p>");
            out.println("</div>");
            out.println("<a href='users?action=test-use-tran' class='btn' style='background:#10b981; margin-right: 10px;'>Chạy Thử Có Dùng Transaction (BTH4)</a>");
            out.println("<a href='users' class='btn'>&larr; Về danh sách User</a>");
            out.println("</div></body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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
