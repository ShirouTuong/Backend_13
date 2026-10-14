<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bài Tập: Gọi MySQL Stored Procedures từ JDBC - BTH5</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #6366f1;
            --primary-hover: #4f46e5;
            --bg: #0b0f19;
            --card-bg: rgba(17, 24, 39, 0.75);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border: rgba(148, 163, 184, 0.12);
            --accent: #06b6d4;
            --success: #10b981;
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
            max-width: 760px;
            background: var(--card-bg);
            backdrop-filter: blur(20px);
            border: 1px solid var(--border);
            border-radius: 28px;
            padding: 48px 44px;
            box-shadow: 0 25px 60px -15px rgba(0, 0, 0, 0.6);
            text-align: center;
        }

        .badge {
            display: inline-block;
            padding: 6px 16px;
            background: rgba(99, 102, 241, 0.15);
            color: #a5b4fc;
            border: 1px solid rgba(129, 140, 248, 0.3);
            border-radius: 9999px;
            font-size: 0.82rem;
            font-weight: 600;
            margin-bottom: 24px;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }

        h1 {
            font-size: 2.25rem;
            font-weight: 800;
            line-height: 1.25;
            margin-bottom: 14px;
            background: linear-gradient(135deg, #ffffff 30%, #94a3b8 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        p.desc {
            color: var(--text-muted);
            font-size: 1.05rem;
            line-height: 1.65;
            margin-bottom: 32px;
        }

        .actions {
            display: flex;
            gap: 16px;
            justify-content: center;
            flex-wrap: wrap;
            margin-bottom: 36px;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 14px 28px;
            border-radius: 14px;
            font-weight: 600;
            font-size: 0.98rem;
            text-decoration: none;
            transition: all 0.25s ease;
            cursor: pointer;
        }

        .btn-primary {
            background: var(--primary);
            color: #ffffff;
            box-shadow: 0 10px 24px -6px rgba(99, 102, 241, 0.5);
        }

        .btn-primary:hover {
            background: var(--primary-hover);
            transform: translateY(-2px);
            box-shadow: 0 14px 28px -6px rgba(99, 102, 241, 0.65);
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.06);
            color: var(--text-main);
            border: 1px solid var(--border);
        }

        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.12);
            transform: translateY(-2px);
        }

        .sp-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px;
            text-align: left;
            margin-top: 24px;
            padding-top: 28px;
            border-top: 1px solid var(--border);
        }

        .sp-card {
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid var(--border);
            border-radius: 14px;
            padding: 16px;
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .sp-card-title {
            font-size: 0.92rem;
            font-weight: 600;
            color: #38bdf8;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .sp-card-desc {
            font-size: 0.82rem;
            color: var(--text-muted);
            line-height: 1.45;
        }

        .tag-sp {
            display: inline-block;
            font-size: 0.72rem;
            padding: 2px 8px;
            background: rgba(16, 185, 129, 0.15);
            color: var(--success);
            border-radius: 6px;
            font-weight: 600;
            font-family: monospace;
        }
    </style>
</head>
<body>
    <div class="container">
        <span class="badge">[Bài tập] JDBC + MySQL CallableStatement</span>
        <h1>Quản Lý Người Dùng BTH5</h1>
        <p class="desc">
            Toàn bộ các chức năng CRUD được thực thi thông qua <strong>MySQL Stored Procedures</strong> và <strong>JDBC CallableStatement</strong> theo đúng yêu cầu bài tập.
        </p>

        <div class="actions">
            <a href="${pageContext.request.contextPath}/users" class="btn btn-primary">
                Xem Danh Sách Users (get_all_users)
            </a>
            <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-secondary">
                + Thêm User Mới (insert_user)
            </a>
        </div>

        <div class="sp-grid">
            <div class="sp-card">
                <div class="sp-card-title">
                    <span class="tag-sp">PROCEDURE</span> get_all_users()
                </div>
                <div class="sp-card-desc">Hiển thị danh sách tất cả users bằng CallableStatement.</div>
            </div>

            <div class="sp-card">
                <div class="sp-card-title">
                    <span class="tag-sp">PROCEDURE</span> update_user(...)
                </div>
                <div class="sp-card-desc">Sửa thông tin user (tên, email, quốc gia) theo ID.</div>
            </div>

            <div class="sp-card">
                <div class="sp-card-title">
                    <span class="tag-sp">PROCEDURE</span> delete_user(id)
                </div>
                <div class="sp-card-desc">Xoá người dùng theo ID thông qua CallableStatement.</div>
            </div>

            <div class="sp-card">
                <div class="sp-card-title">
                    <span class="tag-sp">PROCEDURE</span> get_user_by_id(id)
                </div>
                <div class="sp-card-desc">Lấy dữ liệu người dùng nạp vào form chỉnh sửa.</div>
            </div>
        </div>
    </div>
</body>
</html>
