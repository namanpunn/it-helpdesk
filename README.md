Part 1: README - First Time Setup
Create: it-helpdesk-system/SETUP.md
markdown# 🚀 First Time Setup Guide

## Prerequisites Check

Before starting, ensure you have:

- [ ] **Java 17+** installed
```bash
  java -version
  # Should show: java version "17" or higher
```

- [ ] **Maven 3.6+** installed
```bash
  mvn -version
  # Should show: Apache Maven 3.6.x or higher
```

- [ ] **Docker Desktop** installed and running
```bash
  docker --version
  # Should show: Docker version 20.x or higher
```

- [ ] **Git** installed
```bash
  git --version
```

---

## Step-by-Step Setup (5 minutes)

### Step 1: Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/it-helpdesk-system.git
cd it-helpdesk-system
```

### Step 2: Start Firestore Emulator
```bash
# From project root directory
docker-compose up -d

# Verify it's running
docker ps
# Should show firestore-emulator container running on port 8086
```

### Step 3: Start Ticket Service (Terminal 1)
```bash
cd ticket-service
./mvnw clean install
./mvnw spring-boot:run
```

**Wait for:** `Started TicketServiceApplication in X seconds`

**Then access:**
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Step 4: Start Status Service (Terminal 2 - New Window)
```bash
cd status-service
./mvnw clean install
./mvnw spring-boot:run
```

**Wait for:** `Started StatusServiceApplication in X seconds`

**Then access:**
- Swagger UI: http://localhost:8081/swagger-ui.html
- Health Check: http://localhost:8081/actuator/health

---

## Quick Test - Verify Everything Works

### 1. Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "password123"
  }'
```

**Copy the token from response!**

### 2. Create a Ticket (Replace YOUR_TOKEN)
```bash
curl -X POST http://localhost:8080/tickets/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "employeeId": "EMP001",
    "employeeName": "John Doe",
    "category": "LAPTOP",
    "description": "Laptop running slow and freezing frequently",
    "priority": "HIGH"
  }'
```

**Expected:** 201 Created with ticket details

### 3. View All Tickets
```bash
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  http://localhost:8080/tickets/all
```

**Success!** ✅ If you see tickets, everything is working!

---

## Alternative: Using Swagger UI (Recommended for Demo)

1. **Login:**
   - Go to http://localhost:8080/swagger-ui.html
   - Find `POST /auth/login`
   - Click "Try it out"
   - Use credentials: `john.doe` / `password123`
   - Copy the token

2. **Authorize:**
   - Click the **"Authorize"** button (🔓) at top
   - Enter: `Bearer YOUR_TOKEN`
   - Click "Authorize"

3. **Test APIs:**
   - All endpoints are now authenticated
   - Try creating tickets, updating status, viewing reports

---

## Demo User Credentials

| Username | Password | Role | Use Case |
|----------|----------|------|----------|
| john.doe | password123 | ROLE_USER | Regular employee |
| jane.smith | password123 | ROLE_USER | Regular employee |
| admin | admin123 | ROLE_ADMIN | Administrator |
| it.support | support123 | ROLE_SUPPORT | IT Support staff |

---

## Troubleshooting

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.yml
server:
  port: 8090
```

### Docker Not Running
```bash
# Start Docker Desktop application
# Then run:
docker-compose up -d
```

### Maven Build Fails
```bash
# Clean and rebuild
./mvnw clean install -U
```

### Can't Access Swagger
- Wait 30 seconds after "Started Application" message
- Try: http://localhost:8080/swagger-ui/index.html
- Check logs for errors

---

## Stopping the Application
```bash
# Stop services (Ctrl+C in each terminal)

# Stop Firestore emulator
docker-compose down

# Or stop all Docker containers
docker stop $(docker ps -q)
```

---

## Project Structure
```
it-helpdesk-system/
├── ticket-service/          # Port 8080
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── status-service/          # Port 8081
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml       # Firestore emulator
├── postman/                 # API collections
├── screenshots/             # Demo screenshots
└── README.md
```

---

## Next Steps

1. ✅ Review API documentation: http://localhost:8080/swagger-ui.html
2. ✅ Import Postman collection from `postman/` folder
3. ✅ Run unit tests: `./mvnw test`
4. ✅ Check SLA reports: `GET /tickets/sla/report`
5. ✅ Explore status tracking: http://localhost:8081/swagger-ui.html

---

**Total Setup Time: 5-10 minutes**

Happy Testing! 🎉