# ===========================================
# Azure Deployment Helper Script
# ===========================================
# Script to automate Azure resource creation for laundry-locker-backend
# Run this script in PowerShell after logging into Azure CLI
#
# Prerequisites:
#   1. Azure CLI installed (winget install Microsoft.AzureCLI)
#   2. Docker Desktop running
#   3. Logged into Azure (az login)
# ===========================================

# Configuration - CHANGE THESE VALUES
$RESOURCE_GROUP = "laundry-locker-rg"
$LOCATION = "southeastasia"
$ACR_NAME = "laundrylockeracr"  # Must be globally unique, lowercase only
$APP_SERVICE_PLAN = "laundry-locker-plan"
$WEB_APP_NAME = "laundry-locker-api"  # Must be globally unique
$SKU = "B1"  # F1 for free, B1 for basic ($13/mo)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Azure Deployment Script for Laundry Locker" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check Azure CLI login
Write-Host "[1/7] Checking Azure CLI login..." -ForegroundColor Yellow
$account = az account show 2>$null | ConvertFrom-Json
if (-not $account) {
    Write-Host "Not logged in. Running 'az login'..." -ForegroundColor Red
    az login
}
Write-Host "Logged in as: $($account.user.name)" -ForegroundColor Green
Write-Host "Subscription: $($account.name)" -ForegroundColor Green
Write-Host ""

# Create Resource Group
Write-Host "[2/7] Creating Resource Group..." -ForegroundColor Yellow
az group create --name $RESOURCE_GROUP --location $LOCATION --output table
Write-Host ""

# Create Azure Container Registry
Write-Host "[3/7] Creating Azure Container Registry..." -ForegroundColor Yellow
az acr create `
    --resource-group $RESOURCE_GROUP `
    --name $ACR_NAME `
    --sku Basic `
    --admin-enabled true `
    --output table
Write-Host ""

# Get ACR credentials
Write-Host "[4/7] Getting ACR credentials..." -ForegroundColor Yellow
$ACR_CREDS = az acr credential show --name $ACR_NAME | ConvertFrom-Json
$ACR_USERNAME = $ACR_CREDS.username
$ACR_PASSWORD = $ACR_CREDS.passwords[0].value
Write-Host "ACR Username: $ACR_USERNAME" -ForegroundColor Green
Write-Host "ACR Password: [saved to variable]" -ForegroundColor Green
Write-Host ""

# Build and Push Docker Image
Write-Host "[5/7] Building and pushing Docker image..." -ForegroundColor Yellow
Write-Host "Logging into ACR..." -ForegroundColor Gray
az acr login --name $ACR_NAME

Write-Host "Building Docker image..." -ForegroundColor Gray
docker build -t laundry-locker:latest .

Write-Host "Tagging image..." -ForegroundColor Gray
docker tag laundry-locker:latest "$ACR_NAME.azurecr.io/laundry-locker:latest"

Write-Host "Pushing to ACR..." -ForegroundColor Gray
docker push "$ACR_NAME.azurecr.io/laundry-locker:latest"
Write-Host ""

# Create App Service Plan
Write-Host "[6/7] Creating App Service Plan ($SKU)..." -ForegroundColor Yellow
az appservice plan create `
    --name $APP_SERVICE_PLAN `
    --resource-group $RESOURCE_GROUP `
    --sku $SKU `
    --is-linux `
    --output table
Write-Host ""

# Create Web App
Write-Host "[7/7] Creating Web App..." -ForegroundColor Yellow
az webapp create `
    --resource-group $RESOURCE_GROUP `
    --plan $APP_SERVICE_PLAN `
    --name $WEB_APP_NAME `
    --docker-registry-server-url "https://$ACR_NAME.azurecr.io" `
    --docker-registry-server-user $ACR_USERNAME `
    --docker-registry-server-password $ACR_PASSWORD `
    --deployment-container-image-name "$ACR_NAME.azurecr.io/laundry-locker:latest" `
    --output table
Write-Host ""

# Summary
Write-Host "============================================" -ForegroundColor Green
Write-Host "DEPLOYMENT COMPLETE!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Configure environment variables:" -ForegroundColor White
Write-Host "   az webapp config appsettings set \" -ForegroundColor Gray
Write-Host "     --resource-group $RESOURCE_GROUP \" -ForegroundColor Gray
Write-Host "     --name $WEB_APP_NAME \" -ForegroundColor Gray
Write-Host "     --settings @.env.azure" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Or use Azure Portal:" -ForegroundColor White
Write-Host "   https://portal.azure.com > App Services > $WEB_APP_NAME > Configuration" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Your app will be available at:" -ForegroundColor White
Write-Host "   https://$WEB_APP_NAME.azurewebsites.net" -ForegroundColor Cyan
Write-Host ""
Write-Host "4. View logs with:" -ForegroundColor White
Write-Host "   az webapp log tail --resource-group $RESOURCE_GROUP --name $WEB_APP_NAME" -ForegroundColor Gray
Write-Host ""

# Save credentials for GitHub Actions
Write-Host "============================================" -ForegroundColor Yellow
Write-Host "SAVE THESE FOR GITHUB ACTIONS:" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
Write-Host "ACR_USERNAME: $ACR_USERNAME" -ForegroundColor White
Write-Host "ACR_PASSWORD: $ACR_PASSWORD" -ForegroundColor White
Write-Host ""
Write-Host "For AZURE_CREDENTIALS, run:" -ForegroundColor White
Write-Host "az ad sp create-for-rbac --name github-actions --role contributor --scopes /subscriptions/$($account.id)/resourceGroups/$RESOURCE_GROUP --sdk-auth" -ForegroundColor Gray
