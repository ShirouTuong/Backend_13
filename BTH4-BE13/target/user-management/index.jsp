<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Người Dùng &amp; JDBC Transaction</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --bg: #0f172a;
            --card-bg: rgba(30, 41, 59, 0.75);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border: rgba(148, 163, 184, 0.15);
            --accent: #06b6d4;
            --success: #10b981;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
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
            max-width: 740px;
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
            padding: 6px 16px;
            background: rgba(79, 70, 229, 0.2);
            color: #818cf8;
            border: 1px solid rgba(129, 140, 248, 0.3);
            border-radius: 9999px;
            font-size: 0.85rem;
            font-weight: 700;
            margin-bottom: 20px;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }
        h1 {
            font-size: 2.2rem;
            font-weight: 800;
            line-height: 1.3;
            margin-bottom: 16px;
            background: linear-gradient(135deg, #ffffff 0%, #cbd5e1 100%);
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
            gap: 14px;
            justify-content: center;
            flex-wrap: wrap;
        }
        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 13px 24px;
            border-radius: 12px;
            font-weight: 600;
            font-size: 0.95rem;
            text-decoration: none;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
        }
        .btn-primary {
            background: var(--primary);
            color: #ffffff;
            box-shadow: 0 10px 20px -5px rgba(79, 70, 229, 0.5);
        }
        .btn-primary:hover { background: var(--primary-hover); transform: translateY(-2px); }
        .btn-tran {
            background: rgba(16, 185, 129, 0.2);
            color: #6ee7b7;
            border: 1px solid rgba(16, 185, 129, 0.4);
        }
        .btn-tran:hover { background: rgba(16, 185, 129, 0.3); transform: translateY(-2px); }
        .btn-secondary {
            background: rgba(255, 255, 255, 0.06);
            color: var(--text-main);
            border: 1px solid var(--border);
        }
        .btn-secondary:hover { background: rgba(255, 255, 255, 0.12); transform: translateY(-2px); }
        .feature-list {
            margin-top: 36px;
            padding-top: 28px;
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
            flex-shrink: 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <span class="badge">[Thực Hành] JDBC Transaction</span>
        <h1>Thực Thi SQL Có Sử Dụng Transaction</h1>
        <p class="desc">
            Vận dụng thành thạo JDBC Transaction để kiểm soát quá trình ghi dữ liệu, đảm bảo nguyên lý <b>All-or-Nothing</b> (thành công tất cả hoặc hủy bỏ toàn bộ khi có lỗi) qua <code>conn.setAutoCommit(false)</code>, <code>commit()</code> và tự động rollback.
        </p>

        <div class="actions">
            <a href="${pageContext.request.contextPath}/users" class="btn btn-primary">
                Xem Danh Sách Users
            </a>
            <a href="${pageContext.request.contextPath}/users?action=test-use-tran" class="btn btn-tran">
                &#9889; Test Có Dùng Transaction (BTH4)
            </a>
            <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-secondary">
                + Thêm Mới User
            </a>
        </div>

        <div class="feature-list">
            <div class="feature-item">
                <span class="dot"></span>
                <span>Tắt tự động lưu: <code>conn.setAutoCommit(false)</code></span>
            </div>
            <div class="feature-item">
                <span class="dot"></span>
                <span>Xác nhận giao dịch: <code>conn.commit()</code></span>
            </div>
            <div class="feature-item">
                <span class="dot"></span>
                <span>Tự động rollback khi gặp ngoại lệ</span>
            </div>
            <div class="feature-item">
                <span class="dot"></span>
                <span>Không phát sinh rác dữ liệu trong MySQL</span>
            </div>
        </div>
    </div>
</body>
</html>