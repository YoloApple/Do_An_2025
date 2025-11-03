# API Gateway (Kong)

API Gateway cho hệ thống microservices, sử dụng Kong với JWT authentication.

## 🚀 Quick Setup

### 1. Tạo file `kong.yml` từ template

```bash
# Copy template
cp kong.yml.template kong.yml
```

### 2. Cập nhật secrets trong `kong.yml`

Mở file `kong.yml` và thay thế các PLACEHOLDER:

```yaml
consumers:
  - username: auth-service-consumer
    jwt_secrets:
      - key: "auth-service"  # <-- Copy JWT_ISSUER từ auth_service/.env
        secret: "your-real-jwt-secret-here"  # <-- Copy JWT_SECRET từ auth_service/.env
        algorithm: HS256
```

**Lấy giá trị từ đâu?**

```bash
# Xem JWT_SECRET và JWT_ISSUER
cat ../auth_service/.env | grep JWT
```

### 3. Cập nhật URL của Course Service (nếu có)

```yaml
services:
  - name: course-service
    url: https://arrased-contrate-shonta.ngrok-free.dev  # <-- Cập nhật URL này
```

### 4. Khởi động Kong Gateway

```bash
docker-compose up -d
```

Chờ 30 giây để Kong khởi động.

### 5. (Tùy chọn) Khởi động ngrok

```bash
.\start-ngrok.ps1
```

---

## 🔐 Security

### ❌ Files KHÔNG được commit:
- `kong.yml` - Chứa JWT_SECRET và các secrets thực tế

### ✅ Files PHẢI commit:
- `kong.yml.template` - Template với placeholder values
- `.gitignore` - Đảm bảo kong.yml không bị commit
- `docker-compose.yml` - Docker configuration
- `README.md` - File này

---

## 📋 Workflow

### Khi Course Service URL thay đổi:

1. Mở file `kong.yml`
2. Tìm dòng `url: https://...`
3. Thay bằng URL mới
4. Restart Kong: `docker-compose restart kong`

### Khi thêm service mới:

1. Mở file `kong.yml.template` để xem cấu trúc
2. Thêm service mới vào `kong.yml` (không phải template!)
3. Restart Kong: `docker-compose restart kong`
4. (Tùy chọn) Cập nhật template nếu muốn share cấu trúc với team

---

## 🧪 Testing

```bash
# Login để lấy token
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Test protected endpoint
curl http://localhost:8000/api/courses \
  -H "Authorization: Bearer <your-token>" \
  -H "ngrok-skip-browser-warning: true"
```

---

## 🆘 Troubleshooting

### "No credentials found for given 'iss'"

**Nguyên nhân:** JWT_ISSUER trong `kong.yml` không khớp với Auth Service.

**Giải pháp:**
```bash
# Kiểm tra JWT_ISSUER của Auth Service
cat ../auth_service/.env | grep JWT_ISSUER

# Cập nhật trong kong.yml consumer
# - key: "auth-service"  # <-- Giá trị này phải khớp với JWT_ISSUER
```

### "no Route matched"

**Nguyên nhân:** Kong chưa đọc được `kong.yml`.

**Giải pháp:**
```bash
# Kiểm tra kong.yml có tồn tại không
ls -la kong.yml

# Nếu không có, tạo từ template
cp kong.yml.template kong.yml

# Restart Kong
docker-compose restart kong
```

### Course Service không hoạt động

**Nguyên nhân:** Ngrok URL đã thay đổi.

**Giải pháp:**
```bash
# Cập nhật URL trong kong.yml
# services:
#   - name: course-service
#     url: https://new-ngrok-url.ngrok-free.dev

# Restart Kong
docker-compose restart kong
```

---

## 👥 Team Workflow

### Khi clone project:

```bash
git clone <repo>
cd api_gateway

# Tạo kong.yml từ template
cp kong.yml.template kong.yml

# Lấy secrets từ Auth Service
cat ../auth_service/.env | grep JWT

# Cập nhật kong.yml với secrets thực tế
# ... edit kong.yml ...

# Start
docker-compose up -d
```

### Khi push code:

```bash
# Chỉ commit template, KHÔNG commit kong.yml
git add kong.yml.template
git commit -m "feat: update gateway configuration template"
git push
```

---

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra `kong.yml` đã được tạo từ template chưa
2. Kiểm tra secrets trong `kong.yml` có đúng không
3. Xem logs: `docker-compose logs -f kong`
4. Verify routes: `curl http://localhost:8001/routes`
