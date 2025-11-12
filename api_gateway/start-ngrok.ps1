Write-Host "Starting ngrok tunnel for Kong Gateway..." -ForegroundColor Green

try {
    $kongStatus = Invoke-RestMethod -Uri "http://localhost:8001/status" -Method Get
    Write-Host "Kong is running" -ForegroundColor Green
} catch {
    Write-Host "Kong is not running. Please start Kong first:" -ForegroundColor Red
    Write-Host "   docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

# ✅ FIXED: Sử dụng domain cố định và trỏ đến đúng port 8000 của Kong
$ngrokDomain = "keitha-unfurred-overconsciously.ngrok-free.dev"
$kongPort = 8000

Write-Host "`nStarting ngrok tunnel for domain '$ngrokDomain' on port $kongPort..." -ForegroundColor Cyan
Write-Host "Please wait..." -ForegroundColor Yellow

$command = "ngrok http --domain=$ngrokDomain $kongPort --log=stdout"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $command

Start-Sleep -Seconds 5

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "ngrok tunnel started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nInstructions for Frontend Developer:" -ForegroundColor Yellow
Write-Host "The API is now available at: https://$ngrokDomain" -ForegroundColor White
Write-Host "Please use this URL as the API_BASE_URL in the frontend." -ForegroundColor White
Write-Host "`nExample URLs:" -ForegroundColor Yellow
Write-Host "   Signup: https://$ngrokDomain/api/v1/auth/signup" -ForegroundColor White
Write-Host "   Login: https://$ngrokDomain/api/v1/auth/login" -ForegroundColor White
Write-Host "   Profile: https://$ngrokDomain/api/v1/me" -ForegroundColor White
Write-Host "`nNote: ngrok free tier has a rate limit of 40 requests/minute." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan