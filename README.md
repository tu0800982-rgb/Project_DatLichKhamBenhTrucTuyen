# MedBooking

## Chạy dự án

1. Tạo cơ sở dữ liệu bằng cách mở MySQL Workbench (hoặc phpMyAdmin) và chạy file `database/schema.sql`.
2. Sao chép `.env.example` thành `.env`, sau đó điền thông tin MySQL của bạn.
3. Cài thư viện: `npm install`.
4. Chạy dự án: `npm start`.
5. Mở `http://localhost:3000`.

Không mở trực tiếp file HTML bằng Live Server: ứng dụng cần chạy qua Node.js để frontend gọi được API và backend kết nối MySQL.
