<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh Sách Người Dùng - MySQL Stored Procedures (BTH5)</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #6366f1;
            --primary-hover: #4f46e5;
            --danger: #ef4444;
            --danger-hover: #dc2626;
            --bg: #0b0f19;
            --card-bg: rgba(17, 24, 39, 0.75);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border: rgba(148, 163, 184, 0.12);
            --table-header: rgba(30, 41, 59, 0.6);
            --row-hover: rgba(30, 41, 59, 0.4);
            --badge-sp: #10b981;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
        }

        body {
            min-height: 100vh;
            background: radial-gradient(circle at 80% 20%, #1e1b4b 0%, #0b0f19 70%);
            color: var(--text-main);
            padding: 40px 20px;
        }

        .container {
            max-width: 1080px;
            margin: 0 auto;
        }

        .header-bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
            flex-wrap: wrap;
            gap: 16px;
        }

        .title-group h1 {
            font-size: 1.85rem;
            font-weight: 700;
            letter-spacing: -0.02em;
        }

        .title-group p {
            color: var(--text-muted);
            font-size: 0.95rem;
            margin-top: 4px;
        }

        .sp-badge {
            display: inline-block;
            padding: 4px 10px;
            background: rgba(16, 185, 129, 0.15);
            color: var(--badge-sp);
            border: 1px solid rgba(16, 185, 129, 0.3);
            border-radius: 6px;
            font-size: 0.78rem;
            font-weight: 600;
            margin-left: 8px;
            vertical-align: middle;
        }

        .banner-sp {
            background: rgba(99, 102, 241, 0.08);
            border: 1px solid rgba(99, 102, 241, 0.2);
            border-radius: 12px;
            padding: 12px 18px;
            margin-bottom: 20px;
            font-size: 0.88rem;
            color: #c7d2fe;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 10px;
        }

        .banner-sp code {
            font-family: monospace;
            background: rgba(0, 0, 0, 0.3);
            padding: 2px 6px;
            border-radius: 4px;
            color: #38bdf8;
        }

        .actions-top {
            display: flex;
            gap: 12px;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 20px;
            border-radius: 10px;
            font-size: 0.92rem;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
        }

        .btn-primary {
            background: var(--primary);
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }

        .btn-primary:hover {
            background: var(--primary-hover);
            transform: translateY(-1px);
        }

        .btn-ghost {
            background: rgba(255, 255, 255, 0.05);
            color: var(--text-main);
            border: 1px solid var(--border);
        }

        .btn-ghost:hover {
            background: rgba(255, 255, 255, 0.1);
        }

        .card {
            background: var(--card-bg);
            backdrop-filter: blur(16px);
            border: 1px solid var(--border);
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.5);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }

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

        tbody tr:hover {
            background: var(--row-hover);
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        .user-id {
            color: var(--text-muted);
            font-weight: 600;
            font-size: 0.88rem;
        }

        .user-name {
            font-weight: 600;
            color: var(--text-main);
        }

        .user-email {
            color: #38bdf8;
        }

        .action-cell {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .btn-sm {
            padding: 6px 14px;
            border-radius: 8px;
            font-size: 0.85rem;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s ease;
        }

        .btn-edit {
            background: rgba(99, 102, 241, 0.2);
            color: #a5b4fc;
            border: 1px solid rgba(165, 180, 252, 0.3);
        }

        .btn-edit:hover {
            background: var(--primary);
            color: #ffffff;
        }

        .btn-delete {
            background: rgba(239, 68, 68, 0.15);
            color: #fca5a5;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        .btn-delete:hover {
            background: var(--danger);
            color: #ffffff;
        }

        .empty-state {
            padding: 60px 20px;
            text-align: center;
            color: var(--text-muted);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-bar">
            <div class="title-group">
                <h1>Quản Lý Người Dùng <span class="sp-badge">MySQL Stored Procedures</span></h1>
                <p>Toàn bộ thao tác Danh Sách, Thêm, Sửa, Xoá sử dụng CallableStatement</p>
            </div>
            <div class="actions-top">
                <a href="${pageContext.request.contextPath}/" class="btn btn-ghost">Trang Chủ</a>
                <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-primary">+ Thêm Mới User</a>
            </div>
        </div>

        <div class="banner-sp">
            <span>⚡ Danh sách đang được tải qua Stored Procedure: <code>CALL get_all_users()</code></span>
            <span>Xoá qua: <code>CALL delete_user(?)</code> | Sửa qua: <code>CALL update_user(...)</code></span>
        </div>

        <div class="card">
            <table>
                <thead>
                    <tr>
                        <th style="width: 80px;">ID</th>
                        <th>Họ và Tên</th>
                        <th>Email</th>
                        <th>Quốc Gia</th>
                        <th style="width: 200px;">Hành Động</th>
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
                                        <div class="action-cell">
                                            <a class="btn-sm btn-edit" href="${pageContext.request.contextPath}/users?action=edit&id=${user.id}" title="Gọi get_user_by_id và update_user">
                                                Sửa (SP)
                                            </a>
                                            <a class="btn-sm btn-delete" href="${pageContext.request.contextPath}/users?action=delete&id=${user.id}" onclick="return confirm('Bạn có chắc chắn muốn gọi Stored Procedure delete_user để xóa user #${user.id} (${user.name}) không?');" title="Gọi delete_user">
                                                Xoá (SP)
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="5" class="empty-state">
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
