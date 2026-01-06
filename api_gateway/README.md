# API Gateway (Kong)

API Gateway cho hệ thống microservices, sử dụng **Kong Gateway** (DB-less mode) để quản lý routing và authentication (JWT).

## 📋 Mục lục
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt & Chạy](#cài-đặt--chạy)
- [Cấu trúc Project](#cấu-trúc-project)
- [Cấu hình Chi tiết](#cấu-hình-chi-tiết)
- [Các lệnh thường dùng](#các-lệnh-thường-dùng)
- [Troubleshooting](#troubleshooting)

## Yêu cầu hệ thống
- Docker Desktop
- Docker Compose
- Make (optional, nhưng khuyến nghị để chạy các lệnh tắt)

## Cài đặt & Chạy

### 1. Chuẩn bị file cấu hình
Kong chạy ở chế độ **Declarative Config (DB-less)**, nghĩa là toàn bộ cấu hình nằm trong file `kong.yml`.

> ⚠️ **QUAN TRỌNG:** File `kong.yml` chứa secret keys nên **KHÔNG** được commit lên git. Bạn phải tạo nó từ template.

```bash
# Copy từ file mẫu
cp kong.yml.template kong.yml
```

### 2. Cập nhật Secrets
Mở file `kong.yml` vừa tạo và cập nhật các thông tin JWT secret để khớp với `auth_service`.

Lấy thông tin từ `auth_service`:
```bash
cat ../auth_service/.env | grep JWT
```

Cập nhật vào `kong.yml`:
```yaml
consumers:
  - username: auth-service-consumer
    jwt_secrets:
      - key: "auth-service"             # <-- Giá trị JWT_ISSUER
        secret: "your-secret-key-here"  # <-- Giá trị JWT_SECRET
        algorithm: HS256
```

### 3. Khởi chạy
Sử dụng Docker Compose để chạy Kong:

```bash
docker-compose up -d
```

Hoặc nếu dùng windows scripts có sẵn:
```powershell
.\kong-setup.ps1
```

Sau khi chạy xong, Kong sẽ lắng nghe tại:
- **Proxy Port**: `http://localhost:8000` (API Requests đi qua cổng này)
- **Admin API**: `http://localhost:8001`
- **Admin GUI**: `http://localhost:8002` (Nếu bật)

## Cấu trúc Project
```
api_gateway/
├── kong.yml.template     # File mẫu cấu hình Kong (Commit file này)
├── kong.yml             # File cấu hình chính thức (KHÔNG commit file này)
├── docker-compose.yml   # Định nghĩa container Kong
├── Makefile             # Các lệnh rút gọn (make up, make logs...)
├── kong-setup.ps1       # Script setup cho Windows
└── README.md            # Tài liệu hướng dẫn
```

## Cấu hình Chi tiết

### Routing
Các routes được định nghĩa trong `kong.yml` dưới phần `services`.

Ví dụ route cho `auth-service`:
```yaml
services:
  - name: auth-service
    url: http://host.docker.internal:8088  # Trỏ về Auth Service chạy local
    routes:
      - name: auth-routes
        paths:
          - /api/v1/auth
```

### Authentication
Để bảo vệ một route bằng JWT:
1. Enable plugin `jwt` cho service hoặc route đó.
2. Client phải gửi header: `Authorization: Bearer <token>`

## Các lệnh thường dùng

Nếu bạn có cài `make`:

| Lệnh | Ý nghĩa |
|------|---------|
| `make up` | Khởi động Gateway |
| `make down` | Tắt Gateway |
| `make restart` | Khởi động lại (cần thiết khi sửa `kong.yml`) |
| `make logs` | Xem logs realtime |
| `make setup` | Chạy script setup ban đầu |

Nếu không dùng `make`, bạn dùng docker-compose thuần:
```bash
docker-compose up -d
docker-compose restart kong
docker-compose logs -f kong
```

## Troubleshooting

### 1. Lỗi "no Route matched with those values"
- Nguyên nhân: Kong chưa load được file `kong.yml` hoặc file cấu hình sai.
- Khắc phục:
  - Kiểm tra xem file `kong.yml` đã tồn tại chưa.
  - Chạy `docker-compose restart kong` để load lại config.

### 2. Lỗi "Unauthorized" hoặc "Invalid token"
- Nguyên nhân: `JWT_SECRET` trong `kong.yml` không khớp với `auth_service`.
- Khắc phục: Copy lại chính xác `JWT_SECRET` từ `.env` của `auth_service` vào `kong.yml`.

### 3. Không kết nối được tới Service con (502 Bad Gateway)
- Nguyên nhân: Kong trong Docker không nhìn thấy service chạy ở localhost máy host.
- Khắc phục: Trong `kong.yml`, hãy dùng `url: http://host.docker.internal:PORT` thay vì `localhost`.
