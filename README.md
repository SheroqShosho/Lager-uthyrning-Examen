# Lageruthyrning - Examensarbete

En fullstack-applikation för att hyra ut lagerutrymmen. Projektet är byggt med Java Spring Boot för backend och React/Vite för frontend.

## 🔗 Snabblänkar

- 🏠 **Frontend:** http://localhost:5173
- 🔧 **Backend:** http://localhost:8080
- 🗄️ **Databas (PhpMyAdmin):** http://localhost:8082
- 📊 **Admin-panelen:** Logga in och klicka på "Admin Panel"

---

## ⚡ Starta projektet på en ny enhet (SNABBVERSION)

**Förutsättningar:** 
- Docker Desktop installerad
- Node.js installerad (v16+)
- Git installerad (för att clona projektet)

**Steg-för-steg:**

**1. Klona projektet**
```bash
git clone <repo-url>
cd Lageruthyrning-Examen
```

**2. Starta databas och backend i Terminal 1**
```bash
cd docker
docker-compose up
```
*Vänta tills du ser "Started LageruthyrningExamenApplication" i loggen*

**3. Starta frontend i Terminal 2 (öppna ny terminal)**
```bash
cd frontend
npm install
npm run dev
```
*Vänta tills du ser "VITE v..." i loggen*

**4. Öppna webbläsaren**
- Frontend: `http://localhost:5173`
- Admin-panel: `http://localhost:8082` (PhpMyAdmin för databas)

**5. Logga in som admin**
- **Email:** `admin@gmail.com`
- **Lösenord:** `admin123`

Eller **skapa ett nytt konto** och logga in som vanlig användare.

---

## 🚀 Snabbstart

### Förutsättningar
- **Java 17+** (backend)
- **Node.js 16+** (för frontend under development)
- **Docker & Docker Compose** (för databaser och backend i container)
- **MySQL 8** (körs på port 3307)

---

## 🐳 Docker Setup (REKOMMENDERAT)

**Det enklaste sättet att köra hela systemet:**

### Terminal 1 - Start databas och backend
```bash
cd docker
docker-compose up
```

Denna terminal visar loggar från:
- ✅ **MySQL Database** (port 3307) 
- ✅ **PhpMyAdmin** (port 8082) - databaskonfiguration
- ✅ **Backend Spring Boot** (port 8080)

**Vänta tills du ser:** `Started LageruthyrningExamenApplication` i loggen

### Terminal 2 - Start frontend
Öppna en **ny terminal** i projektmappen och kör:
```bash
cd frontend
npm install
npm run dev
```

Frontend körs på: **http://localhost:5173**

**Vänta tills du ser:** `VITE v...` och `➜  Local: http://localhost:5173/`

### Stänga Docker
För att stoppa allt, gå till Terminal 1 och tryck `Ctrl+C`

---

## 🔧 Manuell Setup (Utan Docker - INTE REKOMMENDERAT)

Om du vill köra backend lokalt utan Docker behöver du:
1. **MySQL 8** installerat och igång lokalt
2. **Database** `lageruthyrning_db` skapad manuellt
3. En separat terminal för backend

> ⚠️ **Vi rekommenderar Docker** - det är mycket enklare!

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
- Host: `localhost:3307` (eller 3306 beroende på MySQL-setup)
- Database: `lageruthyrning_db`
- User: `lageruser`
- Password: `lagerpass123`

> 💡 **Tips:** Använd Docker istället - då behövs ingen manuell MySQL-installation!

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

Databasen `lageruthyrning_db` skapas **automatiskt** av Hibernate när Docker startar (se `spring.jpa.hibernate.ddl-auto=create` i application.properties).

Databasen innehåller:
- **users** - Användarkonton (admin och customers)
- **storage_units** - Lagerutrymmen för uthyrning
- **bookings** - Bokningar från användare
- **booking_items** - Individuella items/enheter i en bokning

### Seed-data (data.sql)

Filen `backend/src/main/resources/data.sql` innehåller:
- ✅ **En admin-användare** (`admin@gmail.com` / `admin123`)
- ✅ **5 test-lagerutrymmen** med olika storlekar och priser

Denna data läses in automatiskt vid varje start av Docker (på grund av `spring.sql.init.mode=always`). Det betyder att databasen nollställs och fylls med testdata varje gång.

### Databastabeller (skapas automatiskt av Hibernate)

**users**
```sql
id (BIGINT, PRIMARY KEY)
email (VARCHAR, UNIQUE)
password (VARCHAR, hashed)
full_name (VARCHAR)
role (ENUM: 'ADMIN', 'CUSTOMER')
created_at (TIMESTAMP)
```

**storage_units**
```sql
id (BIGINT, PRIMARY KEY)
name (VARCHAR)
description (TEXT)
size_m2 (DECIMAL)              -- Storlek i kvadratmeter
price_per_day (DECIMAL)
location (VARCHAR)
is_active (BOOLEAN)
created_at (TIMESTAMP)
```

**bookings**
```sql
id (BIGINT, PRIMARY KEY)
user_id (BIGINT, FOREIGN KEY)
payment_status (ENUM: 'PENDING', 'COMPLETED')
total_price (DECIMAL)
created_at (TIMESTAMP)
```

**booking_items**
```sql
id (BIGINT, PRIMARY KEY)
booking_id (BIGINT, FOREIGN KEY)
storage_unit_id (BIGINT, FOREIGN KEY)
start_date (DATE)
end_date (DATE)
```

---

## 🔄 Hur data hanteras

### Vid varje Docker-start
1. Hibernate skapar alla tabeller (från Entity-klasser)
2. Spring läser `data.sql` och fyller databasen med testdata
3. Gamla data raderas före ny seed (använd `SET FOREIGN_KEY_CHECKS`)

### Test-data som initieras
- **Admin-användare:** `admin@gmail.com` (lösenord: `admin123`, hashed)
- **5 lagerutrymmen:** Olika storlekar (15-100 m²) och priser (149-599 kr/dag)

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
    "description": "Litet lagerutrymme",
    "sizeSqm": 50.0,
    "pricePerDay": 199.99,
    "location": "Malmö"
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

## 🔑 Miljövariabler & Konfiguration

### Backend Configuration (`backend/src/main/resources/application.properties`)

```properties
# Server
server.port=8080

# Database (när körning via Docker)
spring.datasource.url=jdbc:mysql://lager_db:3306/lageruthyrning_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=lageruser
spring.datasource.password=lagerpass123

# Hibernate - Skapar tabeller automatiskt vid start
spring.jpa.hibernate.ddl-auto=create
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# JWT Secret (för autentisering)
app.jwt.secret=YOUR_VERY_LONG_RANDOM_SECRET_FOR_DEVELOPMENT_ONLY_MUST_BE_AT_LEAST_64_CHARS

# Logging
logging.level.root=INFO
logging.level.se.gritacademy.lageruthyrningexamen=DEBUG
```

### Förklaring av viktiga inställningar:

- **`spring.jpa.hibernate.ddl-auto=create`** - Hibernate skapar alla tabeller automatiskt vid start. **Databasen nollställs varje gång!**
- **`spring.sql.init.mode=always`** - Kör `data.sql` automatiskt efter tabell-skapandet
- **`lager_db:3306`** - Det är DNS-namn inom Docker-nätverket (inte localhost!)

### Docker Configuration (`docker/docker-compose.yml`)

```yaml
MYSQL_DATABASE: lageruthyrning_db
MYSQL_USER: lageruser
MYSQL_PASSWORD: lagerpass123

# Portar:
# - MySQL: 3307 (externt) → 3306 (internt)
# - PhpMyAdmin: 8082
# - Backend: 8080
```

### Frontend Configuration (`frontend/src/api.js`)

```javascript
const API_BASE = "http://localhost:8080"; // Backend URL
```

> ✅ **Frontend kommunicerar alltid med Backend på port 8080**

---

## 🐛 Troubleshooting

### "MySQL command not recognized"
**Problem:** `mysql: The term 'mysql' is not recognized...`

**Lösning:** Du behöver inte installera MySQL manuellt! Använd Docker istället:
```bash
cd docker
docker-compose up
```
Docker startar MySQL automatiskt. Du behöver bara Node.js och Docker.

### Frontend kan inte ansluta till backend (ERR_CONNECTION_REFUSED)
**Problem:** `Failed to load resource: :8080/api/... net::ERR_CONNECTION_REFUSED`

**Lösning:**
1. Kontrollera att Terminal 1 kör `docker-compose up` och visar "Started LageruthyrningExamenApplication"
2. Verifiera att backend körs på port 8080 (kolla Docker-loggen)
3. Starta om Docker: `docker-compose down` och sedan `docker-compose up`

### Databaskonfigurationsfel (Connection refused)
**Problem:** `Communications link failure` eller `Connection refused`

**Lösning:**
1. Kontrollera att Docker är igång: `docker ps`
2. Verifiera Docker-loggen visar att MySQL är startad
3. Starta om Docker: `docker-compose down -v` (radera volymen), sedan `docker-compose up`

### Port redan i användning
**Problem:** `Port 8080 already in use` eller liknande

**Lösning (Windows):**
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### JWT Token verifieringsfel
**Problem:** `401 Unauthorized` på alla autentiserade requests

**Lösning:**
1. Logga ut och logga in igen
2. Kontrollera att Frontend kommunicerar med rätt Backend (http://localhost:8080)
3. Starta om Docker och frontend

### Databasen nollställs varje gång jag startar Docker
**Problem:** Data försvinner när Docker startar om

**Förklaring:** Detta är avsiktligt! `spring.jpa.hibernate.ddl-auto=create` nollställer databasen och fyller den med testdata från `data.sql` varje gång.

**Lösning (om du vill behålla data):**
```properties
# Ändra i backend/src/main/resources/application.properties:
spring.jpa.hibernate.ddl-auto=update
# OBS: Du måste sedan köra SQL manually första gången
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
- **Skapad:** 2025-12-20
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
