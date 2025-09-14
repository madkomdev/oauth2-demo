#!/bin/bash

echo "🚀 Starting Keycloak and supporting services..."

# Create necessary directories
mkdir -p keycloak/import
mkdir -p keycloak/themes

# Copy realm config if it doesn't exist
if [ ! -f keycloak/import/auth-demo-realm.json ]; then
    echo "⚠️  Realm configuration not found. Creating default configuration..."
    echo "The realm will be imported automatically when Keycloak starts."
fi

# Start Docker Compose services
echo "🐳 Starting Docker Compose services..."
docker-compose up -d

echo "📋 Services starting up..."
echo "🔐 Keycloak will be available at: http://localhost:8080"
echo "👤 Admin credentials: admin / admin_password"
echo "🗄️  PostgreSQL (Keycloak): localhost:5433"
echo "🗄️  PostgreSQL (App): localhost:5434"
echo ""

echo "⏳ Waiting for services to be healthy..."

# Wait for Keycloak to be ready
echo "⏳ Waiting for Keycloak to start (this may take a few minutes)..."
timeout=300
counter=0
while ! curl -f http://localhost:8080/realms/master >/dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo "❌ Timeout waiting for Keycloak to start"
        exit 1
    fi
    echo "⏳ Still waiting for Keycloak... ($counter seconds)"
    sleep 10
    counter=$((counter + 10))
done

echo "✅ Keycloak is ready!"
echo ""
echo "🎯 Next steps:"
echo "1. 🌐 Access Keycloak admin console: http://localhost:8080"
echo "2. 🔑 Login with admin/admin_password"
echo "3. 🏢 The 'auth-demo-realm' should be auto-imported"
echo "4. 🔧 Get client secret from auth-demo-client → Credentials tab"
echo "5. 📝 Update application.properties with the client secret"
echo "6. ▶️  Start your Spring Boot app: ./gradlew bootRun"
echo ""
echo "👥 Test users created:"
echo "   - admin/admin123 (ADMIN role)"
echo "   - manager/manager123 (MANAGER role)"
echo "   - user/user123 (USER role)"
echo "   - guest/guest123 (GUEST role)"
echo ""
echo "🧪 Test token endpoint:"
echo "curl -X POST http://localhost:8080/realms/auth-demo-realm/protocol/openid-connect/token \\"
echo "  -H \"Content-Type: application/x-www-form-urlencoded\" \\"
echo "  -d \"grant_type=password&client_id=auth-demo-client&username=user&password=user123\""
