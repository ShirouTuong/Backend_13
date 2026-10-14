<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Người Dùng - JDBC Transaction</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --bg: #0f172a;
            --card-bg: rgba(30, 41, 59, 0.7);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border: rgba(148, 163, 184, 0.15);
            --danger: #ef4444;
            --table-header: rgba(15, 23, 42, 0.6);
            --row-hover: rgba(255, 255, 255, 0.03);
            --success: #10b981;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
        body {
            min-height: 100vh;
            background: radial-gradient(circle at top right, #1e1b4b, #0f172a 60%);
            color: var(--text-main);
            padding: 40px 20px;
        }
        .container { max-width: 1100px; margin: 0 auto; }
        .header-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 32px;
            flex-wrap: wrap;
            gap: 16px;
        }
        .title-group h1 {
            font-size: 1.8rem;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .sp-badge {
            font-size: 0.8rem;
            font-weight: 600;
            padding: 4px 10px;
            background: rgba(79, 70, 229, 0.2);
            color: #818cf8;
            border: 1px solid rgba(129, 140, 248, 0.3);
            border-radius: 9999px;
            text-transform: uppercase;
        }
        .title-group p {
            color: var(--text-muted);
            font-size: 0.95rem;
            margin-top: 4px;
        }
        .actions-top { display: flex; gap: 12px; flex-wrap: wrap; }
        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 18px;
            border-radius: 10px;
            font-size: 0.9rem;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
        }
        .btn-primary {
            background: var(--primary);
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
        }
        .btn-primary:hover { background: var(--primary-hover); transform: translateY(-1px); }
        .btn-tran {
            background: rgba(16, 185, 129, 0.2);
            color: #6ee7b7;
            border: 1px solid rgba(16, 185, 129, 0.4);
        }
        .btn-tran:hover { background: rgba(16, 185, 129, 0.3); transform: translateY(-1px); }
        .btn-ghost {
            background: rgba(255, 255, 255, 0.05);
            color: var(--text-main);
            border: 1px solid var(--border);
        }
        .btn-ghost:hover { background: rgba(255, 255, 255, 0.1); }
        .card {
            background: var(--card-bg);
            backdrop-filter: blur(16px);
            border: 1px solid var(--border);
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.5);
        }
        table { width: 100%; border-collapse: collapse; text-align: left; }
        th {
            background: var(--table-header);
            padding: 16px 20px;
            font-size: 0.82rem;
            font-weight: 700;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            border-bottom: 1px solid var(--border);
        }
        td {
            padding: 16px 20px;
            font-size: 0.95rem;
            border-bottom: 1px solid var(--border);
            vertical-align: middle;
        }
        tbody tr:hover { background: var(--row-hover); }
        tbody tr:last-child td { border-bottom: none; }
        .user-id { color: var(--text-muted); font-weight: 600; font-size: 0.88rem; }
        .user-name { font-weight: 600; color: var(--text-main); }
        .user-email { color: #38bdf8; }
        .action-cell { display: flex; gap: 10px; align-items: center; }
        .btn-sm {
            padding: 6px 14px;
            border-radius: 8px;
            font-size: 0.85rem;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s ease;
        }
        .btn-edit {
            background: rgba(79, 70, 229, 0.2);
            color: #a5b4fc;
            border: 1px solid rgba(165, 180, 252, 0.3);
        }
        .btn-edit:hover { background: var(--primary); color: #ffffff; }
        .btn-delete {
            background: rgba(239, 68, 68, 0.15);
            color: #fca5a5;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }
        .btn-delete:hover { background: var(--danger); color: #ffffff; }
        .empty-state { padding: 60px 20px; text-align: center; color: var(--text-muted); }
        .perm-badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            margin-right: 4px;
            margin-bottom: 4px;
            background: rgba(99, 102, 241, 0.15);
            color: #a5b4fc;
            border: 1px solid rgba(165, 180, 252, 0.25);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-bar">
            <div class="title-group">
                <h1>Quản Lý Người Dùng <span class="sp-badge">MySQL JDBC Transaction</span></h1>
                <p>Danh sách người dùng và phân quyền được kiểm soát an toàn tuyệt đối</p>
            </div>
            <div class="actions-top">
                <a href="${pageContext.request.contextPath}/" class="btn btn-ghost">Trang Chủ</a>
                <a href="${pageContext.request.contextPath}/users?action=test-use-tran" class="btn btn-tran" onclick="return confirm('Chạy kiểm thử có dùng Transaction (thực thi kịch bản All-or-Nothing)?');">&#9889; Test Có Transaction (BTH4)</a>
                <a href="${pageContext.request.contextPath}/users?action=test-without-tran" class="btn" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4);" onclick="return confirm('Chạy kiểm thử KHÔNG dùng Transaction (để so sánh rác dữ liệu)?');">&#9888; Test Không Transaction</a>
                <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-primary">+ Thêm Mới User</a>
            </div>
        </div>

        <div class="card">
            <table>
                <thead>
                    <tr>
                        <th style="width: 70px;">ID</th>
                        <th>Họ và Tên</th>
                        <th>Email</th>
                        <th>Quốc Gia</th>
                        <th style="min-width: 170px;">Quyền Hạn (Permissions)</th>
                        <th style="width: 170px;">Hành Động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty listUser}">
                            <c:forEach var="user" items="${listUser}">
                                <tr>
                                    <td class="user-id">#${user.id}</td>
                                    <td class="user-name">${user.name}</td>
                                    <td class="user-email">${user.email}</td>
                                    <td>${user.country}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty user.permissions}">
                                                <c:forEach var="p" items="${user.permissions}">
                                                    <span class="perm-badge">${p}</span>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--text-muted); font-size: 0.8rem; font-style: italic;">Chưa gán</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="action-cell">
                                            <a class="btn-sm btn-edit" href="${pageContext.request.contextPath}/users?action=edit&id=${user.id}" title="Chỉnh sửa thông tin người dùng">
                                                Sửa
                                            </a>
                                            <a class="btn-sm btn-delete" href="${pageContext.request.contextPath}/users?action=delete&id=${user.id}" onclick="return confirm('Bạn có chắc chắn muốn xóa người dùng này không?');">
                                                Xóa
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="empty-state">
                                    Không có người dùng nào trong cơ sở dữ liệu. Nhấn "Thêm Mới User" để thêm.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>