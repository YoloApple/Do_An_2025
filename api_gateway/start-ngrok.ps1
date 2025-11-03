Write-Host "Starting ngrok tunnel for Kong Gateway..." -ForegroundColor Green

try {
    $kongStatus = Invoke-RestMethod -Uri "http://localhost:8001/status" -Method Get
    Write-Host "Kong is running" -ForegroundColor Green
} catch {
    Write-Host "Kong is not running. Please start Kong first:" -ForegroundColor Red
    Write-Host "   docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "`nStarting ngrok tunnel on port 8000..." -ForegroundColor Cyan
Write-Host "Please wait..." -ForegroundColor Yellow

Start-Process powershell -ArgumentList "-NoExit", "-Command", "ngrok http 8000 --log=stdout"

Start-Sleep -Seconds 5

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "ngrok tunnel started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nInstructions for Frontend Developer:" -ForegroundColor Yellow
Write-Host "1. Open ngrok dashboard: http://localhost:4040" -ForegroundColor White
Write-Host "2. Copy the 'Forwarding' HTTPS URL (e.g., https://xxxx.ngrok-free.app)" -ForegroundColor White
Write-Host "3. Use that URL as API_BASE_URL in frontend" -ForegroundColor White
Write-Host "`nExample URLs:" -ForegroundColor Yellow
Write-Host "   Signup: https://xxxx.ngrok-free.app/api/v1/auth/signup" -ForegroundColor White
Write-Host "   Login: https://xxxx.ngrok-free.app/api/v1/auth/login" -ForegroundColor White
Write-Host "   Profile: https://xxxx.ngrok-free.app/api/v1/me" -ForegroundColor White
Write-Host "`nNote: ngrok free tier có giới hạn 40 requests/minute" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan