#!/bin/bash

# Web Application Testing Script
echo "🌐 Testing Web Application Flow"
echo "==============================="

# Check if Keycloak is running
if ! curl -f http://localhost:8080/realms/master >/dev/null 2>&1; then
    echo "❌ Keycloak is not running. Please start it first:"
    echo "   ./start-services.sh"
    exit 1
fi

# Check if Spring Boot app is running
if ! curl -f http://localhost:8081/ >/dev/null 2>&1; then
    echo "❌ Spring Boot web app is not running. Please start it:"
    echo "   ./gradlew bootRun"
    exit 1
fi

echo "✅ Both Keycloak and Spring Boot app are running"
echo ""

echo "🌐 Web Application URLs:"
echo "========================"
echo "🏠 Landing Page:     http://localhost:8081/"
echo "🔐 Login (Keycloak): http://localhost:8081/oauth2/authorization/keycloak"
echo "📊 Dashboard:        http://localhost:8081/dashboard (requires login)"
echo "👤 Profile:          http://localhost:8081/profile (requires login)"
echo "👨‍💼 Admin Panel:       http://localhost:8081/admin (requires ADMIN role)"
echo ""
echo "📱 API Endpoints:"
echo "=================="
echo "🌍 Public Health:    http://localhost:8081/api/public/health"
echo "ℹ️  Public Info:      http://localhost:8081/api/public/info"
echo ""

echo "👥 Test Users for Login:"
echo "========================"
echo "🔴 admin/admin123     (ADMIN role - full access)"
echo "🟡 manager/manager123 (MANAGER role - manager + user access)"
echo "🟢 user/user123       (USER role - user access only)"
echo "⚪ guest/guest123     (GUEST role - limited access)"
echo ""

echo "🧪 Testing Public Endpoints:"
echo "============================"

echo "📊 Testing /api/public/health..."
HEALTH_RESULT=$(curl -s http://localhost:8081/api/public/health)
if echo "$HEALTH_RESULT" | jq . >/dev/null 2>&1; then
    echo "✅ Health Check: $(echo "$HEALTH_RESULT" | jq -r '.status')"
else
    echo "❌ Health Check failed"
fi

echo "ℹ️  Testing /api/public/info..."
INFO_RESULT=$(curl -s http://localhost:8081/api/public/info)
if echo "$INFO_RESULT" | jq . >/dev/null 2>&1; then
    echo "✅ Info: $(echo "$INFO_RESULT" | jq -r '.description')"
else
    echo "❌ Info endpoint failed"
fi

echo ""
echo "🌐 Web Application Flow Test:"
echo "============================"

echo "📱 Testing landing page..."
LANDING_RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/)
if [ "$LANDING_RESULT" = "200" ]; then
    echo "✅ Landing page accessible"
else
    echo "❌ Landing page failed (HTTP $LANDING_RESULT)"
fi

echo "🔒 Testing protected page (should redirect to Keycloak)..."
DASHBOARD_RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/dashboard)
if [ "$DASHBOARD_RESULT" = "302" ] || [ "$DASHBOARD_RESULT" = "401" ]; then
    echo "✅ Dashboard properly protected (HTTP $DASHBOARD_RESULT)"
else
    echo "❌ Dashboard protection failed (HTTP $DASHBOARD_RESULT)"
fi

echo ""
echo "🎯 Next Steps:"
echo "=============="
echo "1. 🌐 Open your browser and go to: http://localhost:8081/"
echo "2. 🔐 Click 'Login with Keycloak' to test the OAuth2 flow"
echo "3. 👤 Login with one of the test users (e.g., user/user123)"
echo "4. 📊 Explore the dashboard and test API calls"
echo "5. 👨‍💼 Try logging in as admin/admin123 to access the admin panel"
echo ""
echo "💡 OAuth2 Flow:"
echo "==============="
echo "Landing Page → Login Button → Keycloak → Authentication → Redirect → Dashboard"
echo ""
echo "🔧 Troubleshooting:"
echo "==================="
echo "- If login fails, check the client secret in application.properties"
echo "- If APIs return 401, the JWT token might be expired"
echo "- Check browser developer tools for detailed error messages"
echo "- Use H2 console: http://localhost:8081/h2-console"
echo ""
echo "🏁 Web application is ready for testing!"
