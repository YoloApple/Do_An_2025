# Auth Service

Microservice chịu trách nhiệm xác thực (Authentication) và phân quyền (Authorization) cho hệ thống.

## 📋 Công nghệ sử dụng
- **Java:** 21
- **Framework:** Spring Boot 3.3.0
- **Database:** PostgreSQL
- **Build Tool:** Gradle
- **Security:** Spring Security + OAuth2 (Google) + JWT
- **Email:** Resend API

## 🚀 Yêu cầu môi trường
- JDK 21 trở lên
- PostgreSQL (đang chạy)

## ⚙️ Cấu hình (Environment Variables)

Dự án sử dụng file `.env` để quản lý biến môi trường.
Copy file `.env.example` (nếu có) hoặc tạo mới file `.env` tại thư mục gốc `auth_service/` với nội dung sau:

```properties
# Database
PGHOST=localhost
PGPORT=5432
PGDATABASE=auth_db
PGUSER=postgres
PGPASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your_very_long_secure_secret_key_at_least_64_chars # Key dùng để ký token
JWT_ISSUER=auth-service                                      # Tên issuer (phải khớp với api_gateway)
JWT_ACCESS_EXP_MINUTES=60                                    # Thời hạn Access Token (phút)
JWT_REFRESH_EXP_DAYS=14                                      # Thời hạn Refresh Token (ngày)

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
# URL redirect sau khi login Google thành công (thường là FE hoặc endpoint debug)
OAUTH2_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# Email Service (Resend)
RESEND_API_KEY=re_123456789...
RESEND_API_FROM=onboarding@resend.dev
RESEND_API_REPLYTO=support@example.com

# Frontend
APP_FRONTEND_URL=http://localhost:3000

# Server Port
PORT=8088
```

## 🛠️ Cài đặt & Chạy ứng dụng

### 1. Clone và Setup
```bash
git clone <repo_url>
cd auth_service
```

### 2. Tạo Database
Tạo database PostgreSQL rỗng tên là `auth_db` (hoặc tên bạn đặt trong `.env`). Hibernate sẽ tự động tạo bảng khi chạy lần đầu.

### 3. Chạy ứng dụng (Windows)
```powershell
.\gradlew.bat bootRun
```

### 4. Chạy ứng dụng (Mac/Linux)
```bash
./gradlew bootRun
```

Sau khi chạy thành công, server sẽ lắng nghe tại `http://localhost:8088`.

## 📚 API Documentation

Project có tích hợp sẵn Swagger/OpenAPI. Sau khi chạy server, truy cập:

- **Swagger UI:** [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)
- **OpenAPI JSON:** [http://localhost:8088/v3/api-docs](http://localhost:8088/v3/api-docs)

## 🐳 Docker Deployment

Để build Docker image:

```bash
docker build -t auth-service .
```

Để chạy với Docker (cần truyền file env):

```bash
docker run -p 8088:8080 --env-file .env auth-service
```

## 🔐 Luồng xác thực (Authentication Flow)

1. **Login:** User gửi username/password -> Trả về `accessToken` và `refreshToken`.
2. **Access Resource:** Client gửi `Authorization: Bearer <accessToken>` lên Gateway.
3. **Gateway Verify:** Gateway kiểm tra chữ ký token bằng `JWT_SECRET`.
4. **Refresh Token:** Khi accessToken hết hạn, dùng `refreshToken` để lấy pair mới.

## 📝 Troubleshooting

**Lỗi kết nối Database:**
- Kiểm tra lại `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD` trong file `.env`.
- Đảm bảo PostgreSQL đang chạy.

**Lỗi gửi mail:**
- Kiểm tra `RESEND_API_KEY`.
- Tài khoản Resend miễn phí chỉ gửi được cho chính email đăng ký (trừ khi add domain).

**Lỗi JWT Signature:**
- Đảm bảo `JWT_SECRET` trong `.env` của `auth_service` **KHỚP HOÀN TOÀN** với secret khai báo trong `kong.yml` của API Gateway.
