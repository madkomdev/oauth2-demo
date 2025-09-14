#!/bin/bash

# API Testing Script
echo "🧪 API Testing Script"
echo ""

# Check if services are running
if ! curl -f http://localhost:8080/realms/master >/dev/null 2>&1; then
    echo "❌ Keycloak is not running. Please start it first:"
    echo "   ./start-services.sh"
    exit 1
fi

if ! curl -f http://localhost:8081/api/public/health >/dev/null 2>&1; then
    echo "❌ Spring Boot app is not running. Please start it:"
    echo "   ./gradlew bootRun"
    exit 1
fi

echo "✅ Both Keycloak and Spring Boot app are running"
echo ""

# Test public endpoints
echo "🌐 Testing public endpoints..."
echo ""
echo "📊 Health check:"
curl -s http://localhost:8081/api/public/health | jq .
echo ""

echo "ℹ️  Application info:"
curl -s http://localhost:8081/api/public/info | jq .
echo ""

# Function to get token
get_token() {
    local username=$1
    local password=$2
    curl -s -X POST http://localhost:8080/realms/auth-demo-realm/protocol/openid-connect/token \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password&client_id=auth-demo-client&username=$username&password=$password" \
      | jq -r '.access_token'
}

echo "🔐 Testing with different user roles..."
echo ""

# Test with USER role
echo "👤 Testing with USER role (user/user123)..."
USER_TOKEN=$(get_token "user" "user123")
if [ "$USER_TOKEN" != "null" ] && [ "$USER_TOKEN" != "" ]; then
    echo "✅ User token obtained"
    echo "📋 User profile:"
    curl -s -H "Authorization: Bearer $USER_TOKEN" http://localhost:8081/api/user/profile | jq .
    echo ""
else
    echo "❌ Failed to get user token"
fi

# Test with MANAGER role
echo "👔 Testing with MANAGER role (manager/manager123)..."
MANAGER_TOKEN=$(get_token "manager" "manager123")
if [ "$MANAGER_TOKEN" != "null" ] && [ "$MANAGER_TOKEN" != "" ]; then
    echo "✅ Manager token obtained"
    echo "📊 Manager reports:"
    curl -s -H "Authorization: Bearer $MANAGER_TOKEN" http://localhost:8081/api/manager/reports | jq .
    echo ""
else
    echo "❌ Failed to get manager token"
fi

# Test with ADMIN role
echo "👨‍💼 Testing with ADMIN role (admin/admin123)..."
ADMIN_TOKEN=$(get_token "admin" "admin123")
if [ "$ADMIN_TOKEN" != "null" ] && [ "$ADMIN_TOKEN" != "" ]; then
    echo "✅ Admin token obtained"
    echo "🔧 System info:"
    curl -s -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8081/api/admin/system/info | jq .
    echo ""
else
    echo "❌ Failed to get admin token"
fi

echo "🏁 Testing completed!"
echo ""
echo "💡 Available endpoints by role:"
echo "🌐 Public: /api/public/*"
echo "👤 User: /api/user/*"  
echo "👔 Manager: /api/manager/* + /api/user/*"
echo "👨‍💼 Admin: /api/admin/* + /api/manager/* + /api/user/*"
