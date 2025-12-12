# Databasdesign – Lageruthyrning

## Översikt tabeller

- `users` – lagrar användare (kunder/admin)
- `storage_units` – lagrar hyrbara lagerutrymmen
- `bookings` – representerar en genomförd bokning (order)
- `booking_items` – enskilda lagerutrymmen kopplade till en bokning
- `door_events` – logg av öppna/låsa-händelser för hyrda utrymmen

### Tabell: users

Lagrar systemets användare (kunder och ev. admin).

| Kolumn       | Typ            | Info                                |
|-------------|----------------|-------------------------------------|
| id          | BIGINT PK      | Auto-increment                      |
| email       | VARCHAR(255)   | Unikt, används för inloggning       |
| password    | VARCHAR(255)   | Hashat lösenord                     |
| full_name   | VARCHAR(255)   | Användarens namn                    |
| role        | VARCHAR(50)    | T.ex. `CUSTOMER` eller `ADMIN`      |
| created_at  | TIMESTAMP      | När användaren skapades             |

### Tabell: storage_units

Lagrar lagerutrymmen (rum/förråd) som kan hyras.

| Kolumn        | Typ            | Info                                  |
|---------------|----------------|---------------------------------------|
| id            | BIGINT PK      | Auto-increment                        |
| name          | VARCHAR(100)   | Namn, t.ex. "Förråd A1"               |
| description   | TEXT           | Beskrivning                           |
| size_m2       | DECIMAL(5,2)   | Storlek i kvadratmeter                |
| price_per_day | DECIMAL(10,2)  | Pris per dag                          |
| location      | VARCHAR(255)   | Plats/adress                          |
| is_active     | BOOLEAN        | Om utrymmet är bokningsbart          |
| created_at    | TIMESTAMP      | När utrymmet lades till               |

### Tabell: bookings

Representerar en bokning (order) som gjorts efter mockad betalning.

| Kolumn        | Typ            | Info                                             |
|---------------|----------------|--------------------------------------------------|
| id            | BIGINT PK      | Auto-increment                                   |
| user_id       | BIGINT FK      | Referens till `users.id`                         |
| start_date    | DATE           | Startdatum för hyresperiod                       |
| end_date      | DATE           | Slutdatum för hyresperiod                        |
| total_price   | DECIMAL(10,2)  | Totalt pris för bokningen                        |
| status        | VARCHAR(50)    | T.ex. `PENDING`, `PAID`, `CANCELLED`             |
| created_at    | TIMESTAMP      | När bokningen skapades                           |
| payment_ref   | VARCHAR(100)   | Mockad betalreferens (kan vara null)            |

### Tabell: booking_items

Kopplar en eller flera lagerutrymmen till en bokning.

| Kolumn           | Typ            | Info                                         |
|------------------|----------------|----------------------------------------------|
| id               | BIGINT PK      | Auto-increment                               |
| booking_id       | BIGINT FK      | Referens till `bookings.id`                  |
| storage_unit_id  | BIGINT FK      | Referens till `storage_units.id`             |
| price_per_day    | DECIMAL(10,2)  | Pris per dag vid bokningstillfället (snapshot) |

### Tabell: door_events

Loggar när en användare simulerar öppna/låsa via mockat IoT-API.

| Kolumn       | Typ            | Info                                             |
|--------------|----------------|--------------------------------------------------|
| id           | BIGINT PK      | Auto-increment                                   |
| booking_id   | BIGINT FK      | Referens till `bookings.id`                      |
| action       | VARCHAR(20)    | T.ex. `OPEN` eller `LOCK`                        |
| created_at   | TIMESTAMP      | När åtgärden utfördes                            |

## Relationer (ER-modell)

- En `user` kan ha många `bookings`
    - `users.id` ↔ `bookings.user_id` (1:N)

- En `booking` kan ha många `booking_items`
    - `bookings.id` ↔ `booking_items.booking_id` (1:N)

- Ett `storage_unit` kan förekomma i många `booking_items`
    - `storage_units.id` ↔ `booking_items.storage_unit_id` (1:N)

- En `booking` kan ha många `door_events`
    - `bookings.id` ↔ `door_events.booking_id` (1:N)
