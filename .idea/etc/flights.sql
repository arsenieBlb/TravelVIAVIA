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
    local_time TIME
);

CREATE TABLE flight(
    flight_id INTEGER PRIMARY KEY,
    carrier_id INTEGER,
    plane_id INTEGER,
    departure_city_id INTEGER,
    arrival_city_id INTEGER,
    departure_time TIMESTAMP,
    arrival_time TIMESTAMP,
    base_price DECIMAL(10,3),
    flight_status VARCHAR(10),
    FOREIGN KEY (carrier_id) REFERENCES carrier(carrier_id),
    FOREIGN KEY (plane_id) REFERENCES plane(id),
    FOREIGN KEY (departure_city_id) REFERENCES city(city_id),
    FOREIGN KEY (arrival_city_id) REFERENCES city(city_id)
);

CREATE TABLE booking(
    booking_id INTEGER PRIMARY KEY,
    flight_id INTEGER,
    created_by_customer_id INTEGER,
    passenger_count INTEGER,
    total_price DECIMAL(10, 3),
    FOREIGN KEY (created_by_customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (flight_id) REFERENCES flight(flight_id)
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
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id)
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
    FOREIGN KEY (flight_id) REFERENCES flight(flight_id),
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id)
);

INSERT INTO users (user_id, email, password_hash, user_type) VALUES
(1, 'admin@skyline.com', '1234a', 'Admin'),
(2, 'j.doe@gmail.com', '1234b', 'Customer'),
(3, 'alice.smith@outlook.com', '1234c', 'Customer'),
(4, 'support_team@carrier.com', '1234d', 'Admin'),
(5, 'bob.builder@yahoo.com', '1234e', 'Customer');

INSERT INTO admin (admin_id) VALUES
(1),
(4);

INSERT INTO customer(customer_id, first_name, last_name) VALUES
(2, 'John', 'Doe'),
(3, 'Alice', 'Smith'),
(5, 'Bob', 'Builder');

INSERT INTO carrier(carrier_id, carrier_name) VALUES
(1, 'Global Sky Airways'),
(2, 'Oceanic Airlines');

INSERT INTO luggage_type(luggage_type_id, name, description, max_weight_kg, extra_price) VALUES
(1, 'Carry-on', 'Free under-seat bag', 8, 0.00),
(2, 'Baggage', 'Checked-in baggage', 23, 25.00);

INSERT INTO section(id, num_of_seats, type) VALUES
(1, 20, 'Business'),
(2, 150, 'Economy');

INSERT INTO seat(id, seat_label, section_id) VALUES
(1, '1A', 1),
(2, '1B', 1),
(3, '2A', 1),
(4, '2B', 1),
(101, '10A', 2),
(102, '10B', 2),
(103, '10C', 2),
(104, '11A', 2),
(105, '11B', 2),
(106, '11C', 2);

INSERT INTO plane_type(id, name, plane_section, num_of_columns, model) VALUES
(1, 'Jumbo Jet', 1, 10, 'Boeing 747-8'),
(2, 'Regional Jet', 2, 4, 'Airbus A320');

INSERT INTO plane(id, plane_type, carrier_id) VALUES
(501, 1, 1),
(502, 1, 1),
(701, 2, 2),
(702, 2, 2);

INSERT INTO city(city_id, city_name, country, local_time) VALUES
(1, 'London', 'United Kingdom', 'UTC+1'),
(2, 'Berlin', 'Germany', 'UTC+2'),
(3, 'Paris', 'France', 'UTC+2'),
(4, 'Rome', 'Italy', 'UTC+2'),
(5, 'Madrid', 'Spain', 'UTC+2'),
(6, 'Warsaw', 'Poland', 'UTC+2');

ALTER TABLE city
ALTER COLUMN local_time TYPE VARCHAR(20);

INSERT INTO flight(flight_id, carrier_id, plane_id, departure_city_id,
                   arrival_city_id, departure_time, arrival_time, base_price, flight_status) VALUES
(104, 1, 501, 3, 2,
 '2026-07-10 10:00:00', '2026-07-10 22:00:00', 120.00, 'Available'),
(105, 2, 702, 4, 1,
 '2026-07-11 09:00:00', '2026-07-11 11:30:00', 55.00, 'Available'),
(106, 1, 502, 6, 3,
 '2026-07-12 18:00:00', '2026-07-12 20:15:00', 75.00, 'Unavailable');

ALTER TABLE flight
ALTER COLUMN flight_status TYPE VARCHAR(12);

INSERT INTO booking(booking_id, flight_id, created_by_customer_id, passenger_count, total_price) VALUES
(1, 104, 2, 1, 90.00);

INSERT INTO booking_customer(booking_id, customer_id) VALUES
(1, 2);

INSERT INTO passenger(passenger_id, booking_id, first_name, last_name) VALUES
(2, 1, 'John', 'Doe');

INSERT INTO passenger_luggage(passenger_luggage_id, passenger_id, luggage_type_id, quantity) VALUES
(1, 2, 1, 2),
(2, 2, 2, 1);

INSERT INTO flight_seat(flight_id, seat_id, passenger_id, is_occupied) VALUES
(104, 1, 2, TRUE);
