CREATE SCHEMA flights;

SET SCHEMA 'flights';

CREATE TABLE users(
    user_id INTEGER PRIMARY KEY,
    email VARCHAR(50) UNIQUE ,
    password_hash VARCHAR(50),
    user_type VARCHAR(8)
);

CREATE TABLE admin(
    admin_id INTEGER PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES users(user_id)
);

CREATE TABLE customer(
    customer_id INTEGER PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    FOREIGN KEY (customer_id) REFERENCES users(user_id)
);

CREATE TABLE carrier(
    carrier_id INTEGER PRIMARY KEY,
    carrier_name VARCHAR(100)
);

CREATE TABLE luggage_type(
    luggage_type_id INTEGER PRIMARY KEY,
    name VARCHAR(8),
    description VARCHAR(200),
    max_weight_kg DECIMAL(4,2),
    extra_price DECIMAL(5,2)
);

CREATE TABLE section(
    id INTEGER PRIMARY KEY,
    num_of_seats INTEGER,
    type VARCHAR(8)
);

CREATE TABLE seat(
    id INTEGER PRIMARY KEY ,
    seat_label VARCHAR(5),
    section_id INTEGER,
    FOREIGN KEY (section_id) REFERENCES section(id)
);

CREATE TABLE plane_type(
    id INTEGER PRIMARY KEY ,
    name VARCHAR(100) UNIQUE,
    plane_section INTEGER,
    num_of_columns INTEGER,
    model VARCHAR(20),
    FOREIGN KEY (plane_section) REFERENCES section(id)
);

CREATE TABLE plane(
    id INTEGER PRIMARY KEY,
    plane_type INTEGER,
    carrier_id INTEGER,
    FOREIGN KEY (plane_type) REFERENCES plane_type(id),
    FOREIGN KEY (carrier_id) REFERENCES carrier(carrier_id)
);

CREATE TABLE city(
    city_id INTEGER PRIMARY KEY,
    city_name VARCHAR(100),
    country VARCHAR(100),
local_time INTERVAL);

CREATE TABLE flight(
    flight_id INTEGER PRIMARY KEY,
    carrier_id INTEGER,
    plane_id INTEGER,
    departure_city_id INTEGER,
    arrival_city_id INTEGER,
    departure_time TIMESTAMP,
    arrival_time TIMESTAMP,
    base_price DECIMAL(10,3),
    flight_status VARCHAR(20),
    FOREIGN KEY (carrier_id) REFERENCES carrier(carrier_id),
    FOREIGN KEY (plane_id) REFERENCES plane(id),
    FOREIGN KEY (departure_city_id) REFERENCES city(city_id),
    FOREIGN KEY (arrival_city_id) REFERENCES city(city_id)
);

CREATE TABLE booking(
    booking_id INTEGER PRIMARY KEY,
    flight_id INTEGER,
    second_flight_id INTEGER,
    return_flight_id INTEGER,
    second_return_flight_id INTEGER,
    created_by_customer_id INTEGER,
    passenger_count INTEGER,
    total_price DECIMAL(10, 3),
    FOREIGN KEY (created_by_customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE booking_customer(
    booking_id INTEGER,
    customer_id INTEGER,
    PRIMARY KEY (booking_id, customer_id),
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE passenger(
    passenger_id INTEGER PRIMARY KEY,
    booking_id INTEGER,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    claimed_by_customer_id INTEGER,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id),
    FOREIGN KEY (claimed_by_customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE passenger_luggage(
    passenger_luggage_id INTEGER PRIMARY KEY,
    passenger_id INTEGER,
    luggage_type_id INTEGER,
    quantity INTEGER,
    FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id),
    FOREIGN KEY (luggage_type_id) REFERENCES luggage_type(luggage_type_id)
);

CREATE TABLE flight_seat(
    flight_id INTEGER,
    seat_id INTEGER,
    passenger_id INTEGER,
    is_occupied BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (flight_id, seat_id),
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id)
);

INSERT INTO users (user_id, email, password_hash, user_type) VALUES
(1, 'admin@skyline.com', 'Admin1234', 'Admin'),
(2, 'j.doe@gmail.com', 'Customer1234', 'Customer'),
(3, 'alice.smith@gmail.com', 'Alice1234', 'Customer'),
(4, 'support.team@carrier.com', 'Support1234', 'Admin'),
(5, 'bob.builder@gmail.com', 'Builder1234', 'Customer'),
(6, 'artem.customer@gmail.com', 'Artem1234', 'Customer'),
(7, 'operations.admin@skyline.com', 'Operations1234', 'Admin');

INSERT INTO admin (admin_id) VALUES
(1),
(4),
(7);

INSERT INTO customer(customer_id, first_name, last_name) VALUES
(2, 'John', 'Doe'),
(3, 'Alice', 'Smith'),
(5, 'Bob', 'Builder'),
(6, 'Artem', 'Customer');

INSERT INTO carrier(carrier_id, carrier_name) VALUES
(1, 'Global Sky Airways'),
(2, 'Oceanic Airlines'),
(3, 'Lufthansa'),
(4, 'Ryanair'),
(5, 'Air France'),
(6, 'Wizz Air'),
(7, 'KLM Royal Dutch Airlines');

INSERT INTO luggage_type(luggage_type_id, name, description, max_weight_kg, extra_price) VALUES
(1, 'Carry-on', 'Carry-on bag', 8, 15.00),
(2, 'Baggage', 'Checked-in baggage', 23, 25.00);

INSERT INTO section(id, num_of_seats, type) VALUES
(1, 20, 'Business'),
(2, 150, 'Economy');

INSERT INTO seat(id, seat_label, section_id) VALUES
-- business class rows 1-5 seats A-D
(1, '1A', 1),(2, '1B', 1),(3, '1C', 1),(4, '1D', 1),
(5, '2A', 1),(6, '2B', 1),(7, '2C', 1),(8, '2D', 1),
(9, '3A', 1),(10, '3B', 1),(11, '3C', 1),(12, '3D', 1),
(13, '4A', 1),(14, '4B', 1),(15, '4C', 1),(16, '4D', 1),
(17, '5A', 1),(18, '5B', 1),(19, '5C', 1),(20, '5D', 1),
-- economy class rows 6-30 seats A-F
(21, '6A', 2),(22, '6B', 2),(23, '6C', 2),(24, '6D', 2),(25, '6E', 2),(26, '6F', 2),
(27, '7A', 2),(28, '7B', 2),(29, '7C', 2),(30, '7D', 2),(31, '7E', 2),(32, '7F', 2),
(33, '8A', 2),(34, '8B', 2),(35, '8C', 2),(36, '8D', 2),(37, '8E', 2),(38, '8F', 2),
(39, '9A', 2),(40, '9B', 2),(41, '9C', 2),(42, '9D', 2),(43, '9E', 2),(44, '9F', 2),
(45, '10A', 2),(46, '10B', 2),(47, '10C', 2),(48, '10D', 2),(49, '10E', 2),(50, '10F', 2),
(51, '11A', 2),(52, '11B', 2),(53, '11C', 2),(54, '11D', 2),(55, '11E', 2),(56, '11F', 2),
(57, '12A', 2),(58, '12B', 2),(59, '12C', 2),(60, '12D', 2),(61, '12E', 2),(62, '12F', 2),
(63, '13A', 2),(64, '13B', 2),(65, '13C', 2),(66, '13D', 2),(67, '13E', 2),(68, '13F', 2),
(69, '14A', 2),(70, '14B', 2),(71, '14C', 2),(72, '14D', 2),(73, '14E', 2),(74, '14F', 2),
(75, '15A', 2),(76, '15B', 2),(77, '15C', 2),(78, '15D', 2),(79, '15E', 2),(80, '15F', 2),
(81, '16A', 2),(82, '16B', 2),(83, '16C', 2),(84, '16D', 2),(85, '16E', 2),(86, '16F', 2),
(87, '17A', 2),(88, '17B', 2),(89, '17C', 2),(90, '17D', 2),(91, '17E', 2),(92, '17F', 2),
(93, '18A', 2),(94, '18B', 2),(95, '18C', 2),(96, '18D', 2),(97, '18E', 2),(98, '18F', 2),
(99, '19A', 2),(100, '19B', 2),(101, '19C', 2),(102, '19D', 2),(103, '19E', 2),(104, '19F', 2),
(105, '20A', 2),(106, '20B', 2),(107, '20C', 2),(108, '20D', 2),(109, '20E', 2),(110, '20F', 2),
(111, '21A', 2),(112, '21B', 2),(113, '21C', 2),(114, '21D', 2),(115, '21E', 2),(116, '21F', 2),
(117, '22A', 2),(118, '22B', 2),(119, '22C', 2),(120, '22D', 2),(121, '22E', 2),(122, '22F', 2),
(123, '23A', 2),(124, '23B', 2),(125, '23C', 2),(126, '23D', 2),(127, '23E', 2),(128, '23F', 2),
(129, '24A', 2),(130, '24B', 2),(131, '24C', 2),(132, '24D', 2),(133, '24E', 2),(134, '24F', 2),
(135, '25A', 2),(136, '25B', 2),(137, '25C', 2),(138, '25D', 2),(139, '25E', 2),(140, '25F', 2),
(141, '26A', 2),(142, '26B', 2),(143, '26C', 2),(144, '26D', 2),(145, '26E', 2),(146, '26F', 2),
(147, '27A', 2),(148, '27B', 2),(149, '27C', 2),(150, '27D', 2),(151, '27E', 2),(152, '27F', 2),
(153, '28A', 2),(154, '28B', 2),(155, '28C', 2),(156, '28D', 2),(157, '28E', 2),(158, '28F', 2),
(159, '29A', 2),(160, '29B', 2),(161, '29C', 2),(162, '29D', 2),(163, '29E', 2),(164, '29F', 2),
(165, '30A', 2),(166, '30B', 2),(167, '30C', 2),(168, '30D', 2),(169, '30E', 2),(170, '30F', 2);

INSERT INTO plane_type(id, name, plane_section, num_of_columns, model) VALUES
(1, 'Jumbo Jet', 1, 10, 'Boeing 747-8'),
(2, 'Regional Jet', 2, 4, 'Airbus A320'),
(3, 'Narrow-body', 2, 6, 'Boeing 737-800'),
(4, 'Wide-body', 1, 8, 'Airbus A350'),
(5, 'Turboprop', 2, 4, 'ATR 72');

INSERT INTO plane(id, plane_type, carrier_id) VALUES
(501, 1, 1),
(502, 4, 1),
(701, 2, 2),
(702, 5, 2),
(1001, 3, 3),
(1002, 4, 3),
(1003, 3, 4),
(1004, 3, 4),
(1005, 1, 5),
(1006, 2, 5),
(1007, 3, 6),
(1008, 3, 6),
(1009, 4, 7),
(1010, 2, 7);

INSERT INTO city(city_id, city_name, country, local_time) VALUES
(1, 'London', 'United Kingdom', '1 hour'),
(2, 'Berlin', 'Germany', '2 hours'),
(3, 'Paris', 'France', '2 hours'),
(4, 'Rome', 'Italy', '2 hours'),
(5, 'Madrid', 'Spain', '2 hours'),
(6, 'Warsaw', 'Poland', '2 hours'),
(7, 'Amsterdam', 'Netherlands', '2 hours'),
(8, 'Vienna', 'Austria', '2 hours'),
(9, 'Prague', 'Czechia', '2 hours'),
(10, 'Budapest', 'Hungary', '2 hours'),
(11, 'Stockholm', 'Sweden', '2 hours'),
(12, 'Oslo', 'Norway', '2 hours'),
(13, 'Copenhagen', 'Denmark', '2 hours'),
(14, 'Helsinki', 'Finland', '3 hours'),
(15, 'Athens', 'Greece', '3 hours'),
(16, 'Lisbon', 'Portugal', '1 hour'),
(17, 'Dublin', 'Ireland', '1 hour'),
(18, 'Brussels', 'Belgium', '2 hours'),
(19, 'Zurich', 'Switzerland', '2 hours'),
(20, 'Geneva', 'Switzerland', '2 hours'),
(21, 'Milan', 'Italy', '2 hours'),
(22, 'Barcelona', 'Spain', '2 hours'),
(23, 'Munich', 'Germany', '2 hours'),
(24, 'Frankfurt', 'Germany', '2 hours'),
(25, 'Bucharest', 'Romania', '3 hours'),
(26, 'Sofia', 'Bulgaria', '3 hours'),
(27, 'Belgrade', 'Serbia', '2 hours'),
(28, 'Zagreb', 'Croatia', '2 hours'),
(29, 'Chisinau', 'Moldova', '3 hours'),
(30, 'Riga', 'Latvia', '3 hours'),
(31, 'Tallinn', 'Estonia', '3 hours'),
(32, 'Vilnius', 'Lithuania', '3 hours'),
(33, 'Bratislava', 'Slovakia', '2 hours'),
(34, 'Ljubljana', 'Slovenia', '2 hours'),
(35, 'Reykjavik', 'Iceland', '0 hours');



INSERT INTO flight(flight_id, carrier_id, plane_id, departure_city_id,
                   arrival_city_id, departure_time, arrival_time, base_price, flight_status) VALUES
(104, 1, 501, 3, 2,
 '2026-07-10 10:00:00', '2026-07-10 22:00:00', 120.00, 'Available'),
(105, 2, 702, 4, 1,
 '2026-07-11 09:00:00', '2026-07-11 11:30:00', 55.00, 'Available'),
(106, 1, 502, 6, 3,
 '2026-07-12 18:00:00', '2026-07-12 20:15:00', 75.00, 'Available'),
(107, 3, 1001, 24, 1, '2026-07-15 08:30:00', '2026-07-15 09:15:00', 145.50, 'Available'),
(108, 4, 1003, 17, 3, '2026-07-15 11:00:00', '2026-07-15 13:40:00', 39.99, 'Available'),
(109, 6, 1007, 29, 4, '2026-07-16 06:15:00', '2026-07-16 07:35:00', 85.00, 'Available'),
(110, 5, 1005, 3, 21, '2026-07-16 14:00:00', '2026-07-16 15:30:00', 110.25, 'Available'),
(111, 7, 1009, 7, 11, '2026-07-17 09:45:00', '2026-07-17 11:50:00', 180.00, 'Available'),
(112, 3, 1002, 23, 5, '2026-07-17 16:20:00', '2026-07-17 19:00:00', 210.00, 'Available'),
(113, 4, 1004, 1, 22, '2026-07-18 07:00:00', '2026-07-18 10:15:00', 45.50, 'Available'),
(114, 6, 1008, 25, 2, '2026-07-18 18:30:00', '2026-07-18 19:50:00', 65.00, 'Available'),
(115, 5, 1006, 21, 3, '2026-07-19 08:00:00', '2026-07-19 09:30:00', 105.75, 'Available'),
(116, 7, 1010, 11, 7, '2026-07-19 13:15:00', '2026-07-19 15:20:00', 175.50, 'Available'),
(117, 3, 1001, 1, 24, '2026-07-20 10:30:00', '2026-07-20 13:15:00', 150.00, 'Available'),
(118, 4, 1003, 3, 17, '2026-07-20 14:40:00', '2026-07-20 15:20:00', 35.99, 'Available'),
(119, 6, 1007, 4, 29, '2026-07-21 08:35:00', '2026-07-21 11:55:00', 90.00, 'Available'),
(120, 5, 1005, 3, 8, '2026-07-21 15:00:00', '2026-07-21 17:05:00', 125.00, 'Available'),
(121, 7, 1009, 7, 13, '2026-07-22 10:10:00', '2026-07-22 11:35:00', 88.50, 'Available'),
(122, 3, 1002, 5, 23, '2026-07-22 11:45:00', '2026-07-22 14:15:00', 195.00, 'Available'),
(123, 4, 1004, 22, 1, '2026-07-23 09:20:00', '2026-07-23 10:45:00', 50.00, 'Available'),
(124, 6, 1008, 2, 25, '2026-07-23 20:00:00', '2026-07-23 23:10:00', 70.00, 'Available'),
(125, 5, 1006, 8, 3, '2026-07-24 07:45:00', '2026-07-24 09:50:00', 130.00, 'Available'),
(126, 7, 1010, 13, 7, '2026-07-24 16:30:00', '2026-07-24 17:55:00', 92.50, 'Available'),
(127, 3, 1001, 24, 4, '2026-07-25 08:00:00', '2026-07-25 09:50:00', 160.00, 'Available'),
(128, 4, 1003, 17, 2, '2026-07-25 12:15:00', '2026-07-25 15:30:00', 42.99, 'Available'),
(129, 6, 1007, 29, 21, '2026-07-26 06:45:00', '2026-07-26 08:10:00', 82.00, 'Available'),
(130, 5, 1005, 3, 5, '2026-07-26 14:20:00', '2026-07-26 16:30:00', 115.50, 'Available'),
(131, 7, 1009, 7, 2, '2026-07-27 09:00:00', '2026-07-27 10:20:00', 140.00, 'Available'),
(132, 3, 1002, 4, 24, '2026-07-27 17:10:00', '2026-07-27 19:15:00', 155.00, 'Available'),
(133, 4, 1004, 2, 17, '2026-07-28 07:30:00', '2026-07-28 08:50:00', 38.50, 'Available'),
(134, 6, 1008, 21, 29, '2026-07-28 19:15:00', '2026-07-28 22:30:00', 87.00, 'Available'),
(135, 5, 1006, 5, 3, '2026-07-29 08:40:00', '2026-07-29 10:45:00', 118.00, 'Available'),
(136, 7, 1010, 2, 7, '2026-07-29 13:50:00', '2026-07-29 15:10:00', 135.50, 'Available'),
(137, 3, 1001, 24, 8, '2026-07-30 10:20:00', '2026-07-30 11:45:00', 110.00, 'Available'),
(138, 4, 1003, 17, 5, '2026-07-30 15:00:00', '2026-07-30 18:40:00', 55.99, 'Available'),
(139, 6, 1007, 29, 2, '2026-07-31 06:00:00', '2026-07-31 07:20:00', 75.00, 'Available'),
(140, 5, 1005, 3, 4, '2026-07-31 16:30:00', '2026-07-31 18:40:00', 145.00, 'Available'),
(141, 7, 1009, 7, 1, '2026-08-01 08:15:00', '2026-08-01 08:35:00', 105.00, 'Available'),
(142, 3, 1002, 8, 24, '2026-08-01 12:40:00', '2026-08-01 14:10:00', 115.00, 'Available'),
(143, 4, 1004, 5, 17, '2026-08-02 09:10:00', '2026-08-02 10:55:00', 48.50, 'Available'),
(144, 6, 1008, 2, 29, '2026-08-02 18:45:00', '2026-08-02 22:00:00', 78.00, 'Available'),
(145, 5, 1006, 4, 3, '2026-08-03 07:50:00', '2026-08-03 09:55:00', 150.00, 'Available'),
(146, 7, 1010, 1, 7, '2026-08-03 14:00:00', '2026-08-03 16:15:00', 110.50, 'Available'),
(147, 3, 1001, 24, 3, '2026-08-04 10:05:00', '2026-08-04 11:20:00', 130.00, 'Available'),
(148, 4, 1003, 17, 4, '2026-08-04 13:30:00', '2026-08-04 17:25:00', 60.99, 'Available'),
(149, 6, 1007, 29, 5, '2026-08-05 06:20:00', '2026-08-05 09:45:00', 95.00, 'Available'),
(150, 5, 1005, 3, 1, '2026-08-05 15:45:00', '2026-08-05 16:05:00', 120.00, 'Available'),
(151, 7, 1009, 7, 4, '2026-08-06 09:30:00', '2026-08-06 11:50:00', 165.00, 'Available'),
(152, 3, 1002, 3, 24, '2026-08-06 18:15:00', '2026-08-06 19:35:00', 125.00, 'Available'),
(153, 4, 1004, 4, 17, '2026-08-07 08:25:00', '2026-08-07 10:25:00', 58.50, 'Available'),
(154, 6, 1008, 5, 29, '2026-08-07 20:10:00', '2026-08-08 01:25:00', 89.00, 'Available'),
(155, 5, 1006, 1, 3, '2026-08-08 07:15:00', '2026-08-08 09:35:00', 135.00, 'Available'),
(156, 7, 1010, 4, 7, '2026-08-08 13:40:00', '2026-08-08 16:00:00', 160.50, 'Available'),
(157, 1, 501, 1, 3, '2026-07-20 09:00:00', '2026-07-20 10:30:00', 45.00, 'Available'),
(158, 2, 701, 15, 4, '2026-08-07 15:00:00', '2026-08-07 18:00:00', 95.00, 'Available'),
(159, 1, 501, 9, 24, '2026-08-10 08:00:00', '2026-08-10 09:30:00', 60.00, 'Available'),
(160, 2, 701, 10, 24, '2026-08-10 10:00:00', '2026-08-10 11:30:00', 65.00, 'Available'),
(161, 3, 1001, 12, 24, '2026-08-10 12:00:00', '2026-08-10 14:00:00', 70.00, 'Available'),
(162, 4, 1003, 14, 24, '2026-08-10 14:30:00', '2026-08-10 16:30:00', 75.00, 'Available'),
(163, 5, 1005, 16, 24, '2026-08-10 17:00:00', '2026-08-10 19:30:00', 80.00, 'Available'),
(164, 6, 1007, 18, 24, '2026-08-11 08:00:00', '2026-08-11 09:00:00', 50.00, 'Available'),
(165, 7, 1009, 19, 24, '2026-08-11 10:00:00', '2026-08-11 11:00:00', 55.00, 'Available'),
(166, 1, 502, 20, 24, '2026-08-11 12:00:00', '2026-08-11 13:00:00', 55.00, 'Available'),
(167, 2, 702, 26, 24, '2026-08-11 14:00:00', '2026-08-11 16:00:00', 80.00, 'Available'),
(168, 3, 1002, 27, 24, '2026-08-11 16:30:00', '2026-08-11 18:30:00', 75.00, 'Available'),
(169, 4, 1004, 28, 24, '2026-08-12 08:00:00', '2026-08-12 09:30:00', 70.00, 'Available'),
(170, 5, 1006, 30, 24, '2026-08-12 10:00:00', '2026-08-12 12:00:00', 85.00, 'Available'),
(171, 6, 1008, 31, 24, '2026-08-12 13:00:00', '2026-08-12 15:00:00', 90.00, 'Available'),
(172, 7, 1010, 32, 24, '2026-08-12 16:00:00', '2026-08-12 18:00:00', 85.00, 'Available'),
(173, 1, 501, 33, 24, '2026-08-13 08:00:00', '2026-08-13 09:30:00', 60.00, 'Available'),
(174, 2, 701, 34, 24, '2026-08-13 10:30:00', '2026-08-13 12:00:00', 65.00, 'Available'),
(175, 3, 1001, 35, 24, '2026-08-13 13:00:00', '2026-08-13 17:00:00', 120.00, 'Available');



UPDATE flight SET arrival_time = '2026-07-15 09:55:00' WHERE flight_id = 107;
UPDATE flight SET arrival_time = '2026-07-20 15:55:00' WHERE flight_id = 118;
UPDATE flight SET arrival_time = '2026-08-01 09:25:00' WHERE flight_id = 141;
UPDATE flight SET arrival_time = '2026-08-05 17:00:00' WHERE flight_id = 150;
UPDATE flight SET arrival_time = '2026-08-06 20:00:00' WHERE flight_id = 152;
UPDATE flight SET arrival_time = '2026-08-04 09:05:00' WHERE flight_id = 133;

INSERT INTO booking(booking_id, flight_id, second_flight_id,
                    return_flight_id, second_return_flight_id,
                    created_by_customer_id, passenger_count, total_price) VALUES
(1, 104, NULL, NULL, NULL, 2, 1, 120.00),
(2, 105, NULL, NULL, NULL, 3, 1, 55.00);

INSERT INTO booking_customer(booking_id, customer_id) VALUES
(1, 2),
(2, 3);

INSERT INTO passenger(passenger_id, booking_id, first_name, last_name,
                      claimed_by_customer_id) VALUES
(1, 1, 'John', 'Doe', 2),
(2, 2, 'Mara', 'Linker', NULL);

INSERT INTO passenger_luggage(passenger_luggage_id, passenger_id,
                              luggage_type_id, quantity) VALUES
(1, 1, 1, 1),
(2, 2, 2, 1);

INSERT INTO flight_seat(flight_id, seat_id, passenger_id, is_occupied) VALUES
(104, 21, 1, TRUE),
(105, 22, 2, TRUE);
