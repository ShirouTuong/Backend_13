<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>User Management - MySQL Stored Procedures</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
            rel="stylesheet">
        <style>
            :root {
                --primary: #4f46e5;
                --primary-hover: #4338ca;
                --bg: #0f172a;
                --card-bg: rgba(30, 41, 59, 0.7);
                --text-main: #f8fafc;
                --text-muted: #94a3b8;
                --border: rgba(148, 163, 184, 0.15);
                --accent: #06b6d4;
            }

            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
            }

            body {
                min-height: 100vh;
                background: radial-gradient(circle at top right, #1e1b4b, #0f172a 60%);
                color: var(--text-main);
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 24px;
            }

            .container {
                width: 100%;
                max-width: 680px;
                background: var(--card-bg);
                backdrop-filter: blur(16px);
                border: 1px solid var(--border);
                border-radius: 24px;
                padding: 48px 40px;
                box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
                text-align: center;
            }

            .badge {
                display: inline-block;
                padding: 6px 14px;
                background: rgba(79, 70, 229, 0.2);
                color: #818cf8;
                border: 1px solid rgba(129, 140, 248, 0.3);
                border-radius: 9999px;
                font-size: 0.85rem;
                font-weight: 600;
                margin-bottom: 24px;
                letter-spacing: 0.05em;
                text-transform: uppercase;
            }

            h1 {
                font-size: 2.2rem;
                font-weight: 800;
                line-height: 1.25;
                margin-bottom: 16px;
                background: linear-gradient(135deg, #fff 0%, #cbd5e1 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
            }

            p.desc {
                color: var(--text-muted);
                font-size: 1.05rem;
                line-height: 1.6;
                margin-bottom: 36px;
            }

            .actions {
                display: flex;
                gap: 16px;
                justify-content: center;
                flex-wrap: wrap;
            }

            .btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                padding: 14px 28px;
                border-radius: 12px;
                font-weight: 600;
                font-size: 1rem;
                text-decoration: none;
                transition: all 0.2s ease;
                cursor: pointer;
            }

            .btn-primary {
                background: var(--primary);
                color: #ffffff;
                box-shadow: 0 10px 20px -5px rgba(79, 70, 229, 0.5);
            }

            .btn-primary:hover {
                background: var(--primary-hover);
                transform: translateY(-2px);
                box-shadow: 0 14px 24px -5px rgba(79, 70, 229, 0.6);
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

            .feature-list {
                margin-top: 40px;
                padding-top: 32px;
                border-top: 1px solid var(--border);
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 16px;
                text-align: left;
            }

            .feature-item {
                display: flex;
                align-items: center;
                gap: 10px;
                font-size: 0.9rem;
                color: var(--text-muted);
            }

            .dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: var(--accent);
            }
        </style>
    </head>

    <body>
        <div class="container">
            <span class="badge">[Bài Tập] MySQL JDBC Transaction</span>
            <h1>Luyện Tập JDBC Transaction</h1>
            <p class="desc">
                Luyện tập sử dụng phương thức <code>commit()</code> và <code>rollback()</code> của đối tượng <code>Connection</code>.
                Sửa đổi mã nguồn trong <code>addUserTransaction()</code> để gây ra lỗi SQL, quan sát quá trình rollback tự động để bảo đảm tính <strong>Atomicity (All-or-Nothing)</strong> trong CSDL MySQL.
            </p>

            <div class="actions">
                <a href="${pageContext.request.contextPath}/users" class="btn btn-primary">
                    📋 Quản Lý Người Dùng
                </a>
                <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-secondary">
                    ➕ Thêm Mới User
                </a>
            </div>

            <div style="margin-top: 24px; display: flex; justify-content: center; gap: 12px; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/users?action=test-rollback" class="btn" style="background: rgba(239, 68, 68, 0.2); color: #fca5a5; border: 1px solid rgba(239, 68, 68, 0.4); font-size: 0.9rem; padding: 10px 18px;">
                    ⚡ Test 1: Mô phỏng lỗi SQL (Rollback)
                </a>
                <a href="${pageContext.request.contextPath}/users?action=test-commit" class="btn" style="background: rgba(16, 185, 129, 0.2); color: #6ee7b7; border: 1px solid rgba(16, 185, 129, 0.4); font-size: 0.9rem; padding: 10px 18px;">
                    ✅ Test 2: Chạy bình thường (Commit)
                </a>
            </div>

            <div class="feature-list">
                <div class="feature-item">
                    <span class="dot"></span>
                    <span>Tắt Auto-Commit: <code>connection.setAutoCommit(false)</code></span>
                </div>
                <div class="feature-item">
                    <span class="dot"></span>
                    <span>Ghi nhận dữ liệu: <code>connection.commit()</code></span>
                </div>
                <div class="feature-item">
                    <span class="dot"></span>
                    <span>Hủy bỏ khi lỗi SQL: <code>connection.rollback()</code></span>
                </div>
                <div class="feature-item">
                    <span class="dot"></span>
                    <span>Bảo toàn tính toàn vẹn <strong>ACID / Atomicity</strong></span>
                </div>
            </div>
        </div>
    </body>

    </html>