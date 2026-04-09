-- Seed-data för Lageruthyrning-databasen
-- Denna fil körs automatiskt när Docker startar

-- Rensa gamla data (optional)
DELETE FROM booking_items;
DELETE FROM bookings;
DELETE FROM storage_units;
DELETE FROM users;

-- Skapa test-användare
INSERT INTO users (email, password, full_name, role, created_at) VALUES
('admin@gmail.com', '$2a$10$iJrHNJ8x7kJQXexd3z2cledz1gXdMNyqpNF16566sxcYbifDHYoSa', 'Admin User', 'ADMIN', NOW());

-- Skapa test-lagerutrymmen
INSERT INTO storage_units (name, description, size_m2, price_per_day, location, active, created_at) VALUES
('Lagerrum A1', 'Litet lagerutrymme, perfekt för småföretag', 25.50, 199.99, 'Malmö', TRUE, NOW()),
('Lagerrum A2', 'Medelstor lagring med bra tillgänglighet', 50.00, 349.99, 'Eslöv', TRUE, NOW()),
('Lagerrum B1', 'Stort lagerutrymme för större volymer', 100.00, 599.99, 'Helsingborg', TRUE, NOW()),
('Lagerrum B2', 'Premium lagerutrymme med klimatkontroll', 75.00, 499.99, 'Malmö', TRUE, NOW()),
('Lagerrum C1', 'Kompakt lagring, ekonomisk lösning', 15.00, 149.99, 'Göteborg', TRUE, NOW());

