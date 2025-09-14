# OAuth2 Authentication Demo with Keycloak

A comprehensive Spring Boot application demonstrating OAuth2 authentication and authorization using Keycloak as the OpenID Connect provider. This project showcases production-ready patterns for JWT-based authentication, role-based access control, and microservices architecture.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │  Spring Boot    │    │   Keycloak      │
│   (Client)      │◄──►│  Resource       │◄──►│  Auth Server    │
│                 │    │  Server         │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                          │
                              ▼                          ▼
                       ┌─────────────┐          ┌─────────────┐
                       │     H2      │          │ PostgreSQL  │
                       │  Database   │          │  Database   │
                       └─────────────┘          └─────────────┘
```

## ✨ Key Features

- **🔐 OAuth2 + OpenID Connect** - Complete authentication flow with Keycloak
- **🎭 Role-Based Access Control (RBAC)** - Fine-grained authorization with roles
- **🗝️ JWT Token Processing** - Stateless authentication with custom claims extraction
- **⚡ Session Management** - JWT-based stateless sessions with expiration handling
- **🐳 Docker Compose Setup** - Production-like environment with PostgreSQL
- **🧪 Comprehensive Testing** - Automated API testing with different user roles
- **📊 RESTful APIs** - Sample endpoints demonstrating different authorization levels
- **🔄 User Synchronization** - Automatic user profile creation from JWT tokens

## 🏢 Roles & Permissions

| Role | Description | Accessible Endpoints |
|------|-------------|----------------------|
| **GUEST** | Basic guest access | `/api/public/*` |
| **USER** | Regular authenticated user | `/api/user/*`, `/api/public/*` |
| **MANAGER** | Team/department manager | `/api/manager/*`, `/api/user/*`, `/api/public/*` |
| **ADMIN** | System administrator | `/api/admin/*`, `/api/manager/*`, `/api/user/*`, `/api/public/*` |

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- curl (for testing)
- jq (optional, for JSON formatting)

### 1. Start Keycloak & Database

```bash
# Start all services
./start-services.sh

# Wait for Keycloak to be ready (takes 2-3 minutes)
# ✅ Keycloak will be available at: http://localhost:8080
# 👤 Admin credentials: admin / admin_password
```

### 2. Configure Keycloak Client Secret

```bash
# Get configuration help
./setup-keycloak.sh

# Or manually:
# 1. Go to http://localhost:8080
# 2. Login with admin/admin_password  
# 3. Select "auth-demo-realm"
# 4. Go to Clients → auth-demo-client → Credentials
# 5. Copy the Client Secret
# 6. Update application.properties with the secret
```

### 3. Start Spring Boot Application

```bash
./gradlew bootRun

# Application will start on http://localhost:8081
```

### 4. Test the APIs

```bash
# Run comprehensive API tests
./test-api.sh

# Or test manually:
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/realms/auth-demo-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=auth-demo-client&username=user&password=user123" \
  | jq -r '.access_token')

# Test protected endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/user/profile
```

## 🔗 API Endpoints

### Public Endpoints (No Authentication)
```bash
GET /api/public/health          # Health check
GET /api/public/info           # Application information
```

### User Endpoints (USER+ role required)
```bash
GET /api/user/profile          # User profile
GET /api/user/session          # Session information
GET /api/user/dashboard        # User dashboard
POST /api/user/sync           # Sync user from JWT
```

### Manager Endpoints (MANAGER+ role required)
```bash
GET /api/manager/users         # List all users
GET /api/manager/reports       # Manager reports
GET /api/manager/dashboard     # Manager dashboard
POST /api/manager/users/{id}/roles    # Assign role
DELETE /api/manager/users/{id}/roles  # Remove role
```

### Admin Endpoints (ADMIN role required)
```bash
GET /api/admin/system/info     # System information
GET /api/admin/roles          # List all roles
GET /api/admin/dashboard      # Admin dashboard
POST /api/admin/users/{id}/enable    # Enable user
POST /api/admin/users/{id}/disable   # Disable user
GET /api/admin/audit/sessions        # Active sessions
```

## 👥 Test Users

The system comes with pre-configured test users:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `admin123` | ADMIN | Full system access |
| `manager` | `manager123` | MANAGER | Team management access |
| `user` | `user123` | USER | Regular user access |
| `guest` | `guest123` | GUEST | Limited guest access |

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Kotlin
- **Security**: Spring Security + OAuth2 Resource Server
- **Database**: H2 (development) / PostgreSQL (production)
- **Authentication**: Keycloak (OpenID Connect)
- **Containerization**: Docker Compose
- **Build Tool**: Gradle


## 🔧 Configuration

### Application Properties

Key configuration properties in `src/main/resources/application.properties`:

```properties
# OAuth2 Resource Server (JWT validation)
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/auth-demo-realm

# OAuth2 Client (login flows)
spring.security.oauth2.client.registration.keycloak.client-id=auth-demo-client
spring.security.oauth2.client.registration.keycloak.client-secret=YOUR_CLIENT_SECRET
```

### Database Configuration

- **Development**: H2 in-memory database (default)
- **Production**: PostgreSQL via Docker Compose

To switch to PostgreSQL, uncomment the PostgreSQL configuration and comment out H2 configuration.

## 🐳 Docker Services

The Docker Compose setup includes:

- **Keycloak**: OpenID Connect Provider (port 8080)
- **PostgreSQL (Keycloak)**: Keycloak's database (port 5433)
- **PostgreSQL (App)**: Application database (port 5434)

## 🧪 Testing Strategy

### Manual Testing

1. **Public Endpoints**: No authentication required
2. **Protected Endpoints**: Require valid JWT tokens
3. **Role-based Access**: Different endpoints for different roles


## 🔐 Security Features

### JWT Token Processing

- **Custom Claims Extraction**: Extracts roles from Keycloak JWT tokens
- **Realm Roles**: Supports Keycloak realm-level roles
- **Resource Roles**: Supports client-specific roles
- **Groups Support**: Can extract user groups if configured

### Session Management

- **Stateless Sessions**: No server-side session storage
- **JWT Expiration**: Automatic token expiration handling
- **Session Info**: Detailed session information endpoints

### Authorization Patterns

- **Method-level Security**: `@PreAuthorize` annotations
- **HTTP Security**: Path-based access control
- **Role Hierarchy**: ADMIN > MANAGER > USER > GUEST


## 🛑 Stopping Services

```bash
# Stop all services
./stop-services.sh

# Stop and remove all data
docker-compose down -v
```

