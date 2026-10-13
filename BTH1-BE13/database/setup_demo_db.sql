CREATE DATABASE IF NOT EXISTS demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE demo;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(220) NOT NULL,
    country VARCHAR(120),
    PRIMARY KEY (id)
);

INSERT INTO users (id, name, email, country)
VALUES 
    (1, 'Minh', 'minh@codegym.vn', 'Viet Nam'),
    (2, 'Kante', 'kante@che.uk', 'Kenia')
ON DUPLICATE KEY UPDATE name=VALUES(name);

DROP PROCEDURE IF EXISTS get_user_by_id;
DELIMITER $$
CREATE PROCEDURE get_user_by_id(IN user_id INT)
BEGIN
    SELECT users.name, users.email, users.country
    FROM users
    WHERE users.id = user_id;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS insert_user;
DELIMITER $$
CREATE PROCEDURE insert_user(
    IN user_name VARCHAR(50),
    IN user_email VARCHAR(50),
    IN user_country VARCHAR(50)
)
BEGIN
    INSERT INTO users(name, email, country)
    VALUES(user_name, user_email, user_country);
END$$
DELIMITER ;
