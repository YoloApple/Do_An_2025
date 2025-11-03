Write-Host "🧪 Testing Kong API Gateway..." -ForegroundColor Green

$BASE_URL = "http://localhost:8000"

# Test 1: Health Check qua Kong
Write-Host "`n1️⃣ Testing Health Check..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method Get
    Write-Host "✅ Health check passed" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Signup
Write-Host "`n2️⃣ Testing Signup..." -ForegroundColor Cyan
$signupData = @{
    username = "testuser_$(Get-Random)"
    email = "test_$(Get-Random)@test.com"
    password = "password123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/v1/auth/signup" -Method Post -Body $signupData -ContentType "application/json"
    Write-Host "✅ Signup successful" -ForegroundColor Green
    $global:testUsername = ($signupData | ConvertFrom-Json).username
    $global:testPassword = "password123"
    $global:accessToken = $response.data.accessToken
    Write-Host "Access Token: $($response.data.accessToken.Substring(0,50))..." -ForegroundColor Yellow
} catch {
    Write-Host "❌ Signup failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Login
Write-Host "`n3️⃣ Testing Login..." -ForegroundColor Cyan
$loginData = @{
    username = $global:testUsername
    password = $global:testPassword
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/v1/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    Write-Host "✅ Login successful" -ForegroundColor Green
    $global:accessToken = $response.data.accessToken
    Write-Host "New Access Token: $($response.data.accessToken.Substring(0,50))..." -ForegroundColor Yellow
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get Profile
Write-Host "`n4️⃣ Testing Get Profile (Protected Route)..." -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($global:accessToken)"
    }
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/v1/me" -Method Get -Headers $headers
    Write-Host "✅ Get profile successful" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Get profile failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✅ All tests completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan