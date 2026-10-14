<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Người Dùng - MySQL Stored Procedures (BTH5)</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #6366f1;
            --primary-hover: #4f46e5;
            --bg: #0b0f19;
            --card-bg: rgba(17, 24, 39, 0.75);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border: rgba(148, 163, 184, 0.12);
            --input-bg: rgba(15, 23, 42, 0.6);
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
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }

        .container {
            width: 100%;
            max-width: 560px;
            background: var(--card-bg);
            backdrop-filter: blur(16px);
            border: 1px solid var(--border);
            border-radius: 24px;
            padding: 40px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }

        .header {
            margin-bottom: 28px;
        }

        .sp-badge {
            display: inline-block;
            padding: 4px 10px;
            background: rgba(16, 185, 129, 0.15);
            color: var(--badge-sp);
            border: 1px solid rgba(16, 185, 129, 0.3);
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 600;
            margin-bottom: 12px;
        }

        h2 {
            font-size: 1.6rem;
            font-weight: 700;
            margin-bottom: 6px;
        }

        p.subtitle {
            color: var(--text-muted);
            font-size: 0.9rem;
            line-height: 1.45;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            font-size: 0.88rem;
            font-weight: 600;
            color: var(--text-muted);
            margin-bottom: 8px;
        }

        input[type="text"], input[type="email"] {
            width: 100%;
            padding: 12px 16px;
            background: var(--input-bg);
            border: 1px solid var(--border);
            border-radius: 12px;
            color: var(--text-main);
            font-size: 0.95rem;
            transition: all 0.2s ease;
        }

        input[type="text"]:focus, input[type="email"]:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.25);
        }

        .actions {
            display: flex;
            gap: 12px;
            margin-top: 32px;
        }

        .btn {
            flex: 1;
            padding: 13px;
            border-radius: 12px;
            font-size: 0.95rem;
            font-weight: 600;
            text-align: center;
            text-decoration: none;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
        }

        .btn-primary {
            background: var(--primary);
            color: white;
            box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }

        .btn-primary:hover {
            background: var(--primary-hover);
        }

        .btn-ghost {
            background: rgba(255, 255, 255, 0.05);
            color: var(--text-main);
            border: 1px solid var(--border);
        }

        .btn-ghost:hover {
            background: rgba(255, 255, 255, 0.1);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <span class="sp-badge">CallableStatement: update_user</span>
            <h2>Chỉnh Sửa Người Dùng</h2>
            <p class="subtitle">
                Đọc dữ liệu qua <code>get_user_by_id</code> và lưu cập nhật qua Stored Procedure <code>update_user</code>
            </p>
        </div>

        <form action="${pageContext.request.contextPath}/users" method="post">
            <input type="hidden" name="action" value="edit" />
            <input type="hidden" name="id" value="${user.id}" />

            <div class="form-group">
                <label for="name">Họ và Tên</label>
                <input type="text" id="name" name="name" value="<c:out value='${user.name}' />" required placeholder="Nhập họ và tên..." />
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="<c:out value='${user.email}' />" required placeholder="example@codegym.vn" />
            </div>

            <div class="form-group">
                <label for="country">Quốc Gia</label>
                <input type="text" id="country" name="country" value="<c:out value='${user.country}' />" required placeholder="Nhập quốc gia..." />
            </div>

            <div class="actions">
                <a href="${pageContext.request.contextPath}/users" class="btn btn-ghost">Hủy</a>
                <button type="submit" class="btn btn-primary">Lưu Thay Đổi (update_user)</button>
            </div>
        </form>
    </div>
</body>
</html>
