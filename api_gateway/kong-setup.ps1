Write-Host "🚀 Starting Kong API Gateway setup..." -ForegroundColor Green

# Wait for Kong to be ready
Write-Host "⏳ Waiting for Kong to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

$KONG_ADMIN_URL = "http://localhost:8001"

# Check Kong status
Write-Host "📊 Checking Kong status..." -ForegroundColor Cyan
try {
    $status = Invoke-RestMethod -Uri "$KONG_ADMIN_URL/status" -Method Get
    Write-Host "✅ Kong is ready!" -ForegroundColor Green
} catch {
    Write-Host "❌ Kong is not ready. Please check Docker containers." -ForegroundColor Red
    Write-Host "Run: docker-compose logs kong" -ForegroundColor Yellow
    exit 1
}

# Create Auth Service
Write-Host "`n📝 Creating Auth Service..." -ForegroundColor Cyan
$authService = @{
    name = "auth-service"
    url = "http://host.docker.internal:8080"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/" -Method Post -Body $authService -ContentType "application/json" | Out-Null
    Write-Host "✅ Auth Service created successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Auth Service might already exist" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Create Public Routes
Write-Host "`n🛣️  Creating Public Routes..." -ForegroundColor Cyan
$publicPaths = @(
    "/api/v1/auth/login",
    "/api/v1/auth/signup",
    "/api/v1/auth/refresh",
    "/api/v1/auth/forgot-password",
    "/api/v1/auth/reset-password",
    "/api/v1/auth/logout"
)

$publicRoute = @{
    name = "auth-public-route"
    paths = $publicPaths
    strip_path = $false
    methods = @("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/auth-service/routes" -Method Post -Body $publicRoute -ContentType "application/json" | Out-Null
    Write-Host "✅ Public routes created successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Public routes might already exist" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Create Protected Routes (/me)
Write-Host "`n🔒 Creating Protected Routes..." -ForegroundColor Cyan
$protectedRoute = @{
    name = "me-route"
    paths = @("/api/v1/me")
    strip_path = $false
    methods = @("GET", "PATCH", "PUT", "OPTIONS")
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/auth-service/routes" -Method Post -Body $protectedRoute -ContentType "application/json" | Out-Null
    Write-Host "✅ Protected route created successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Protected route might already exist" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Add CORS Plugin
Write-Host "`n🌍 Adding CORS Plugin..." -ForegroundColor Cyan
$corsPlugin = @{
    name = "cors"
    config = @{
        origins = @(
            "*",
            "http://localhost:3000",
            "https://*.ngrok-free.app",
            "https://*.ngrok.io"
        )
        methods = @("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        headers = @("Accept", "Authorization", "Content-Type", "X-Requested-With", "ngrok-skip-browser-warning")
        credentials = $true
        max_age = 3600
        preflight_continue = $false
    }
} | ConvertTo-Json -Depth 3

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/auth-service/plugins" -Method Post -Body $corsPlugin -ContentType "application/json" | Out-Null
    Write-Host "✅ CORS Plugin added successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️  CORS Plugin might already exist" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Add Rate Limiting Plugin
Write-Host "`n⏱️  Adding Rate Limiting Plugin..." -ForegroundColor Cyan
$rateLimitPlugin = @{
    name = "rate-limiting"
    config = @{
        minute = 100
        hour = 1000
        policy = "local"
    }
} | ConvertTo-Json -Depth 3

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/auth-service/plugins" -Method Post -Body $rateLimitPlugin -ContentType "application/json" | Out-Null
    Write-Host "✅ Rate Limiting Plugin added successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Rate Limiting Plugin might already exist" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Add Request Transformer Plugin
Write-Host "`n🔧 Adding Request Transformer Plugin..." -ForegroundColor Cyan
$transformerPlugin = @{
    name = "request-transformer"
    config = @{
        add = @{
            headers = @("X-Gateway-Source:kong", "X-Forwarded-For:`$remote_addr")
        }
    }
} | ConvertTo-Json -Depth 4

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/auth-service/plugins" -Method Post -Body $transformerPlugin -ContentType "application/json" | Out-Null
    Write-Host "✅ Request Transformer Plugin added successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Request Transformer Plugin might already exist" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✅ Kong setup completed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n📍 Access points:" -ForegroundColor Yellow
Write-Host "   - Kong Proxy: http://localhost:8000" -ForegroundColor White
Write-Host "   - Kong Admin: http://localhost:8001" -ForegroundColor White
Write-Host "   - Konga UI: http://localhost:1337" -ForegroundColor White

Write-Host "`n🧪 Test commands:" -ForegroundColor Yellow
Write-Host "`n1. Signup:" -ForegroundColor Cyan
Write-Host 'curl.exe -X POST http://localhost:8000/api/v1/auth/signup `' -ForegroundColor White
Write-Host '  -H "Content-Type: application/json" `' -ForegroundColor White
Write-Host '  -d "{\"username\":\"testuser\",\"email\":\"test@test.com\",\"password\":\"12345678\"}"' -ForegroundColor White

Write-Host "`n2. Login:" -ForegroundColor Cyan
Write-Host 'curl.exe -X POST http://localhost:8000/api/v1/auth/login `' -ForegroundColor White
Write-Host '  -H "Content-Type: application/json" `' -ForegroundColor White
Write-Host '  -d "{\"username\":\"testuser\",\"password\":\"12345678\"}"' -ForegroundColor White

Write-Host "`n3. Get Profile (requires JWT):" -ForegroundColor Cyan
Write-Host 'curl.exe -X GET http://localhost:8000/api/v1/me `' -ForegroundColor White
Write-Host '  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"' -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Cyan