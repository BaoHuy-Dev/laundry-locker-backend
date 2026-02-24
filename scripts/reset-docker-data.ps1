# ============================================
# Reset Docker Database and Reload Sample Data
# ============================================
# Usage:
#   .\reset-docker-data.ps1           # Reset with confirmation
#   .\reset-docker-data.ps1 -Force    # Reset without confirmation
#   .\reset-docker-data.ps1 -Help     # Show help
# ============================================

param(
    [switch]$Force,
    [switch]$Help
)

# Show help
if ($Help) {
    Write-Host "
============================================
Reset Docker Database Script
============================================

Usage:
  .\reset-docker-data.ps1           Reset with confirmation prompt
  .\reset-docker-data.ps1 -Force    Reset without confirmation
  .\reset-docker-data.ps1 -Help     Show this help message

What this script does:
  1. Stops all Docker containers
  2. Removes PostgreSQL volume (deletes all data)
  3. Restarts all containers
  4. Sample data will be loaded automatically

Note: Redis data and other volumes are preserved.
============================================
"
    exit 0
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Reset Docker Database & Sample Data" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  WARNING: This will DELETE all database data!" -ForegroundColor Yellow
Write-Host ""

# Confirmation
if (-not $Force) {
    $confirm = Read-Host "Are you sure you want to continue? (y/N)"
    if ($confirm -ne "y" -and $confirm -ne "Y") {
        Write-Host ""
        Write-Host "❌ Cancelled." -ForegroundColor Gray
        exit 0
    }
}

Write-Host ""
Write-Host "🛑 Step 1/4: Stopping containers..." -ForegroundColor Cyan
docker-compose down
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Warning: docker-compose down returned error" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🗑️  Step 2/4: Removing PostgreSQL volume..." -ForegroundColor Cyan
$volumeName = "laundry-locker-backend_postgres_data"
docker volume rm $volumeName 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Volume removed: $volumeName" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  Volume not found or already removed" -ForegroundColor Gray
}

Write-Host ""
Write-Host "🚀 Step 3/4: Starting containers..." -ForegroundColor Cyan
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error starting containers!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "⏳ Step 4/4: Waiting for services to initialize..." -ForegroundColor Cyan
Write-Host "   This may take 30-60 seconds..." -ForegroundColor Gray

# Wait and check health
$maxAttempts = 12
$attempt = 0
$healthy = $false

while ($attempt -lt $maxAttempts -and -not $healthy) {
    Start-Sleep -Seconds 5
    $attempt++

    $status = docker inspect --format='{{.State.Health.Status}}' laundry-locker-backend 2>$null

    if ($status -eq "healthy") {
        $healthy = $true
        Write-Host "   ✓ Backend is healthy!" -ForegroundColor Green
    } else {
        Write-Host "   ⏳ Waiting... ($attempt/$maxAttempts)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "📋 Container Status:" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "📜 Recent Backend Logs:" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
docker logs laundry-locker-backend --tail 30 2>&1

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "✅ Done!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "📌 Quick Links:" -ForegroundColor Cyan
Write-Host "   • Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   • API Docs:   http://localhost:8080/v3/api-docs" -ForegroundColor White
Write-Host ""
Write-Host "📌 Commands:" -ForegroundColor Cyan
Write-Host "   • View logs:  docker logs laundry-locker-backend -f" -ForegroundColor White
Write-Host "   • Stop all:   docker-compose down" -ForegroundColor White
Write-Host ""
