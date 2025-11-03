Write-Host "Fixing Kong routes..." -ForegroundColor Green

$KONG_ADMIN_URL = "http://localhost:8001"

# --- 1. Check for auth-service ---
Write-Host "`n[1/4] Checking for 'auth-service'..." -ForegroundColor Cyan
try {
    $service = Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/auth-service" -Method Get -ErrorAction Stop
    Write-Host "  -> Found existing service with ID: $($service.id)" -ForegroundColor Green
} catch {
    Write-Host "  -> Service not found. Creating it now..." -ForegroundColor Yellow
    
    $authServicePayload = @{
        name = "auth-service"
        url  = "http://host.docker.internal:8080"
    } | ConvertTo-Json
    
    try {
        $service = Invoke-RestMethod -Uri "$KONG_ADMIN_URL/services/" -Method Post -Body $authServicePayload -ContentType "application/json" -ErrorAction Stop
        Write-Host "  -> Successfully created service with ID: $($service.id)" -ForegroundColor Green
    } catch {
        Write-Host "  -> FATAL: Failed to create service. Error: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Start-Sleep -Seconds 1

# --- 2. Delete old me-route if it exists ---
Write-Host "`n[2/4] Cleaning up old 'me-route'..." -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$KONG_ADMIN_URL/routes/me-route" -Method Delete -ErrorAction SilentlyContinue
    Write-Host "  -> Old 'me-route' removed." -ForegroundColor Gray
} catch {
    # No old route found, which is fine.
}

Start-Sleep -Seconds 1

# --- 3. Create the new me-route ---
Write-Host "`n[3/4] Creating new 'me-route' for /api/v1/me..." -ForegroundColor Cyan
$meRoutePayload = @{
    name    = "me-route"
    service = @{ id = $service.id }
    paths   = @("/api/v1/me")
    strip_path = $false
    methods = @("GET", "PATCH", "PUT", "OPTIONS")
} | ConvertTo-Json -Depth 3

try {
    $newRoute = Invoke-RestMethod -Uri "$KONG_ADMIN_URL/routes" -Method Post -Body $meRoutePayload -ContentType "application/json" -ErrorAction Stop
    Write-Host "  -> Successfully created route with ID: $($newRoute.id)" -ForegroundColor Green
} catch {
    Write-Host "  -> FATAL: Failed to create route. Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# --- 4. Verify all routes ---
Write-Host "`n[4/4] Verifying all configured routes..." -ForegroundColor Cyan
try {
    $allRoutes = (Invoke-RestMethod -Uri "$KONG_ADMIN_URL/routes").data
    Write-Host "  -> Found $($allRoutes.Count) total routes:"
    foreach ($route in $allRoutes) {
        $serviceName = if ($route.service) { $route.service.name } else { "N/A" }
        Write-Host "     - Name: $($route.name), Path: $($route.paths -join ', '), Service: $serviceName" -ForegroundColor White
    }
} catch {
    Write-Host "  -> Could not verify routes." -ForegroundColor Yellow
}


Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Route configuration completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nTest the route with:" -ForegroundColor Yellow
Write-Host "curl.exe -X GET http://localhost:8000/api/v1/me -H `"Authorization: Bearer YOUR_TOKEN`"" -ForegroundColor White