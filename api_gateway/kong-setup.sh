#!/bin/bash

echo "🚀 Starting Kong API Gateway setup..."

# Wait for Kong to be ready
echo "⏳ Waiting for Kong to be ready..."
sleep 15

KONG_ADMIN_URL="http://localhost:8001"

# Check Kong status
echo "📊 Checking Kong status..."
if curl -s "$KONG_ADMIN_URL/status" > /dev/null; then
    echo "✅ Kong is ready!"
else
    echo "❌ Kong is not ready. Please check Docker containers."
    exit 1
fi

# Create Auth Service
echo ""
echo "📝 Creating Auth Service..."
curl -i -X POST $KONG_ADMIN_URL/services/ \
  --data "name=auth-service" \
  --data "url=http://host.docker.internal:8080"

sleep 2

# Create Public Routes (login, signup, refresh, etc.)
echo ""
echo "🛣️  Creating Public Routes..."
curl -i -X POST $KONG_ADMIN_URL/services/auth-service/routes \
  --data "name=auth-public-route" \
  --data "paths[]=/api/v1/auth/login" \
  --data "paths[]=/api/v1/auth/signup" \
  --data "paths[]=/api/v1/auth/refresh" \
  --data "paths[]=/api/v1/auth/forgot-password" \
  --data "paths[]=/api/v1/auth/reset-password" \
  --data "paths[]=/api/v1/auth/logout" \
  --data "paths[]=/api/v1/auth/oauth2/exchange" \
  --data "paths[]=/oauth2/authorization/google" \
  --data "paths[]=/login/oauth2/code/google" \
  --data "strip_path=false"

sleep 2

# Create Protected Routes (/me endpoint)
echo ""
echo "🔒 Creating Protected Routes..."
curl -i -X POST $KONG_ADMIN_URL/services/auth-service/routes \
  --data "name=me-route" \
  --data "paths[]=/api/v1/me" \
  --data "strip_path=false"

sleep 2

# Add CORS Plugin
echo ""
echo "🌍 Adding CORS Plugin..."
curl -i -X POST $KONG_ADMIN_URL/services/auth-service/plugins \
  --data "name=cors" \
  --data "config.origins=*" \
  --data "config.methods=GET" \
  --data "config.methods=POST" \
  --data "config.methods=PUT" \
  --data "config.methods=DELETE" \
  --data "config.methods=PATCH" \
  --data "config.methods=OPTIONS" \
  --data "config.headers=Accept" \
  --data "config.headers=Authorization" \
  --data "config.headers=Content-Type" \
  --data "config.credentials=true" \
  --data "config.max_age=3600"

sleep 2

# Add Rate Limiting Plugin
echo ""
echo "⏱️  Adding Rate Limiting Plugin..."
curl -i -X POST $KONG_ADMIN_URL/services/auth-service/plugins \
  --data "name=rate-limiting" \
  --data "config.minute=100" \
  --data "config.hour=1000" \
  --data "config.policy=local"

sleep 2

# Add Request Transformer Plugin
echo ""
echo "🔧 Adding Request Transformer Plugin..."
curl -i -X POST $KONG_ADMIN_URL/services/auth-service/plugins \
  --data "name=request-transformer" \
  --data "config.add.headers=X-Gateway-Source:kong"

echo ""
echo "✅ Kong setup completed successfully!"
echo ""
echo "========================================"
echo "📍 Access points:"
echo "   - Kong Proxy: http://localhost:8000"
echo "   - Kong Admin: http://localhost:8001"
echo "   - Konga UI: http://localhost:1337"
echo ""
echo "🧪 Test your API:"
echo '   curl -X POST http://localhost:8000/api/v1/auth/signup \'
echo '     -H "Content-Type: application/json" \'
echo '     -d "{\"username\":\"test\",\"email\":\"test@test.com\",\"password\":\"12345678\"}"'
echo "========================================"