# Lageruthyrning - Examensarbete

En fullstack-applikation för att hyra ut lagerutrymmen. Projektet är byggt med Java Spring Boot för backend och React/Vite för frontend.

## 🔗 Snabblänkar

- 🏠 **Frontend:** http://localhost:5173
- 🔧 **Backend:** http://localhost:8080
- 🗄️ **Databas (PhpMyAdmin):** http://localhost:8082
- 📊 **Admin-panelen:** Logga in och klicka på "Admin Panel"

---

## ⚡ Starta projektet på en ny enhet

**Förutsättningar:** Docker och Node.js måste vara installerade

**Terminal 1:**
```bash
cd docker
docker-compose up
```

**Terminal 2 (ny terminal i samma mapp):**
```bash
cd frontend
npm install
npm run dev
```

**Login:**
- Email: `admin@gmail.com`
- Lösenord: `admin123`

---

## 🚀 Snabbstart

### Förutsättningar
- **Java 17+** (backend)
- **Node.js 16+** (för frontend under development)
- **Docker & Docker Compose** (för databaser och backend i container)
- **MySQL 8** (körs på port 3307)

---

## 🐳 Docker Setup (Rekommenderat)

Det enklaste sättet att köra hela systemet:

### Start alla services
```bash
cd docker
docker-compose up
```

Detta startar automatiskt:
- ✅ **MySQL Database** (port 3307)
- ✅ **PhpMyAdmin** (port 8082) - för databaskonfiguration
- ✅ **Backend Spring Boot** (port 8080)

**Du behöver bara starta frontend separat:**
```bash
cd frontend
npm install
npm run dev
```

Frontend körs på: **http://localhost:5173**

---

## 🔧 Manuell Setup (Utan Docker)

Om du vill köra backend lokalt utan Docker:

### 1. Backend - Starta servern

```bash
cd backend
.\mvnw spring-boot:run
```

Alternativt, om du redan har byggt projektet:
```bash
cd backend
java -jar target/Lageruthyrning-Examen-0.0.1-SNAPSHOT.jar
```

Backend körs på: **http://localhost:8080**

**Databaskonfiguration krävs för manuell körning:**
- Host: `localhost:3307`
- Database: `lageruthyrning_db`
- User: `lageruser`
- Password: `lagerpass123`

### 2. Frontend - Starta webbläsaren

```bash
cd frontend
npm install
npm run dev
```

Frontend körs på: **http://localhost:5173**

---

## 🔐 Admin-inloggning

För att logga in som admin och komma åt admin-panelen:

**Email:** `admin@gmail.com`  
**Lösenord:** `admin123`

> ⚠️ **Obs:** Detta är ett examensarbete utan säkerhetskrav. Lösenorden är enkla och lagras i klartext i koden för demonstration.

---

## 👥 Admin-panelen

Med admin-inloggning får du tillgång till:
- ✅ Skapa nya lagerutrymmen
- ✅ Hantera bokningar
- ✅ Se all systemdata
- ✅ Simulera IoT-lås

Logga in och klicka på "Admin Panel" för att komma till admin-vyn.

---

## 📊 Databasstruktur

Databasen `lageruthyrning_db` innehåller:
- **users** - Användarkonton (admin och customers)
- **storage_units** - Lagerutrymmen för uthyrning
- **bookings** - Bokningar från användare
- **booking_items** - Individuella items/enheter i en bokning

**Databastabeller:**
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE storage_units (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  size_sqm DECIMAL(10,2) NOT NULL,
  price_per_day DECIMAL(10,2) NOT NULL,
  available BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
  total_price DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE booking_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_id BIGINT NOT NULL,
  storage_unit_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  FOREIGN KEY (booking_id) REFERENCES bookings(id),
  FOREIGN KEY (storage_unit_id) REFERENCES storage_units(id)
);
```

---

## 🛠️ Teknikstack

**Backend:**
- Spring Boot 3.5.5
- MySQL 8.4
- JWT-autentisering
- JPA/Hibernate ORM
- Spring Security
- Maven

**Frontend:**
- **React 18** - JavaScript-bibliotek för UI
- **Vite** - Snabb byggtool & dev-server (körs med Node.js)
- **Node.js** - JavaScript-runtime för att köra Vite lokalt (endast under development)
- **Fetch API** - För backend-kommunikation
- **CSS** - Styling

> **Sammanfattning:** React är frontend-koden, Vite är byggverktyget som använder Node.js för att köra en lokal dev-server. Du behöver bara Node.js när du utvecklar. I produktion är det bara statiska filer.

**DevOps:**
- Docker & Docker Compose
- MySQL i container
- PhpMyAdmin för databaskonfiguration

---

## 📝 Projektstruktur

```
Lageruthyrning-Examen/
├── README.md                           # Du läser detta
├── pom.xml                             # Root Maven-konfiguration
├── docker/
│   ├── docker-compose.yml              # Docker-konfiguration för alla services
│   └── Dockerfile                      # Dockerfile för backend
├── backend/                            # Spring Boot backend
│   ├── pom.xml                         # Maven-konfiguration
│   ├── mvnw / mvnw.cmd                 # Maven wrapper
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/se/gritacademy/lageruthyrningexamen/
│   │   │   │   ├── controller/         # REST-endpoints
│   │   │   │   ├── service/            # Affärslogik
│   │   │   │   ├── repository/         # Databaskommunikation
│   │   │   │   ├── model/              # Entity-klasser
│   │   │   │   ├── dto/                # Data Transfer Objects
│   │   │   │   ├── security/           # JWT och säkerhet
│   │   │   │   └── LageruthyrningExamenApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       ├── java/                   # Unit & Integration tests
│   │       └── resources/
│   └── target/
│       └── Lageruthyrning-Examen-0.0.1-SNAPSHOT.jar
└── frontend/                           # React + Vite frontend
    ├── package.json                    # NPM-konfiguration
    ├── vite.config.js                  # Vite-konfiguration
    ├── index.html
    ├── src/
    │   ├── App.jsx                     # Huvudkomponent & routing
    │   ├── AdminPanel.jsx              # Admin-gränssnitt
    │   ├── api.js                      # API-anrop till backend
    │   ├── main.jsx                    # Entry point
    │   ├── index.css                   # Global CSS
    │   ├── App.css                     # App-styling
    │   └── assets/
    └── public/
```

---

## 🔄 API-endpoints

### Autentisering (`/api/auth`)
- `POST /api/auth/login` - Logga in
  ```json
  {
    "email": "user@gmail.com",
    "password": "password123"
  }
  ```
- `POST /api/auth/register` - Registrera ny användare
  ```json
  {
    "email": "newuser@gmail.com",
    "password": "password123"
  }
  ```
- `GET /api/auth/me` - Hämta aktuell inloggad användare (kräver JWT token)

### Lagerutrymmen (`/api/storage-units`)
- `GET /api/storage-units` - Hämta alla tillgängliga lagerutrymmen
- `POST /api/storage-units` - Skapa nytt lagerutrymme (kräver admin)
  ```json
  {
    "name": "Lagerrum A1",
    "sizeSqm": 50.0,
    "pricePerDay": 199.99
  }
  ```

### Bokningar (`/api/bookings`)
- `GET /api/bookings/my` - Hämta mina bokningar (kräver JWT token)
- `POST /api/bookings` - Skapa ny bokning
  ```json
  {
    "items": [
      {
        "storageUnitId": 1,
        "startDate": "2026-04-15",
        "endDate": "2026-04-30"
      }
    ]
  }
  ```

### Admin (`/api/admin`)
- `GET /api/admin/users` - Hämta alla användare (admin only)
- `GET /api/admin/bookings` - Hämta alla bokningar (admin only)
- `GET /api/admin/storage-units` - Hämta alla lagerutrymmen (admin only)

### IoT (`/api/iot`)
- `POST /api/iot/unlock/{storageUnitId}` - Lås upp lagerutrymme (simulerat)
- `POST /api/iot/lock/{storageUnitId}` - Låsa lagerutrymme (simulerat)

---

## 🧪 Testing

### Kör alla tester
```bash
cd backend
.\mvnw test
```

### Kör specifika testklasser
```bash
cd backend
.\mvnw test -Dtest=BookingControllerTest
```

### Tester som ingår
- **Unit Tests** - Modell- och service-tester
- **Integration Tests** - Controller- och repository-tester
- **Test Coverage** - JaCoCo för code coverage

**Testrappport:**
```bash
cd backend
.\mvnw test jacoco:report
# Öppna target/site/jacoco/index.html i webbläsare
```

---

## 🚢 Build & Deployment

### Build backend till JAR
```bash
cd backend
.\mvnw clean package
```

Skapar: `backend/target/Lageruthyrning-Examen-0.0.1-SNAPSHOT.jar`

### Build frontend för produktion
```bash
cd frontend
npm run build
```

Skapar: `frontend/dist/` (produktionsbuild)

### Build Docker-imagen
```bash
cd docker
docker-compose build
```

---

## 🔑 Miljövariabler

### Backend (`backend/src/main/resources/application.properties`)
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3307/lageruthyrning_db
spring.datasource.username=lageruser
spring.datasource.password=lagerpass123
app.jwt.secret=YOUR_VERY_LONG_RANDOM_SECRET_MUST_BE_AT_LEAST_64_CHARS
```

### Docker (`docker/docker-compose.yml`)
```yaml
MYSQL_ROOT_PASSWORD: root
MYSQL_DATABASE: lageruthyrning_db
MYSQL_USER: lageruser
MYSQL_PASSWORD: lagerpass123
JWT_SECRET: YOUR_VERY_LONG_RANDOM_SECRET_MUST_BE_AT_LEAST_64_CHARS
```

---

## 🐛 Troubleshooting

### Frontend kan inte ansluta till backend (ERR_CONNECTION_REFUSED)
**Problem:** `Failed to load resource: :8080/api/... net::ERR_CONNECTION_REFUSED`

**Lösning:**
1. Kontrollera att backend körs på port 8080
2. Verifiera databaskonfiguration i `application.properties`
3. Kolla att Docker-nätverket är konfigurerat korrekt
4. Starta om backend och frontend

### Databaskonfigurationsfel
**Problem:** `Communications link failure with host localhost:3307`

**Lösning:**
1. Verifiera att MySQL körs: `docker ps | grep lager_db`
2. Starta Docker: `docker-compose up -d`
3. Kontrollera portkonfiguration (standard: 3307)
4. Se databasprompt på http://localhost:8082

### Port redan i användning
**Problem:** `Port 8080 already in use` eller liknande

**Lösning:**
```bash
# Hitta process som använder porten (Windows)
netstat -ano | findstr :8080

# Stäng processen
taskkill /PID <PID> /F
```

### JWT Token verifieringsfel
**Problem:** `401 Unauthorized` på alla autentiserade requests

**Lösning:**
1. Kontrollera `app.jwt.secret` är samma överallt
2. Se till att token skickas i Authorization-headern: `Authorization: Bearer <token>`
3. Kontrollera att tokenen inte har förfallit

### Databaskonfiguration reset
**Problem:** Du vill starta om databasen från början

**Lösning:**
```bash
# Radera Docker-volymen
docker-compose down -v

# Starta om
docker-compose up
```

---

## 📌 Viktiga funktioner

✅ **Användarregistrering** - Registrera sig och logga in med JWT  
✅ **Admin-panel** - Skapa och hantera lagerutrymmen  
✅ **Bokningssystem** - Boka lagerutrymmen med datumval  
✅ **JWT-autentisering** - Säker token-baserad inloggning  
✅ **Rollbaserad åtkomst** - Admin och Customer-roller  
✅ **IoT-integration** - Simulera låsning av lagerutrymmen  
✅ **Fullständig testning** - Unit- och integrationstester  
✅ **Docker-deployment** - Containeriserad setup

---

## 📧 Projekt Info

- **Status:** Examensarbete
- **Skapad:** 2026-04-07
- **Version:** 1.0.0
- **Byggverktyg:** Maven (backend), NPM (frontend)
- **Databas:** MySQL 8.4

---

## 🔗 Snabblänkar

- 🏠 Frontend: http://localhost:5173
- 🔧 Backend: http://localhost:8080
- 🗄️ Databas: http://localhost:8082 (PhpMyAdmin)
- 📖 API Documentation: http://localhost:8080/swagger-ui.html (om tillgänglig)

---

**Lycka till med projektet!** 🚀
