CREATE DATABASE IF NOT EXISTS demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE demo;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(220) NOT NULL,
    country VARCHAR(120),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (id, name, email, country)
VALUES 
    (1, 'Minh', 'minh@codegym.vn', 'Viet Nam'),
    (2, 'Kante', 'kante@che.uk', 'Kenia'),
    (3, 'Nguyễn Văn An', 'an.nguyen@example.com', 'Việt Nam'),
    (4, 'John Doe', 'john.doe@company.org', 'United States')
ON DUPLICATE KEY UPDATE 
    name=VALUES(name), 
    email=VALUES(email), 
    country=VALUES(country);

DROP PROCEDURE IF EXISTS get_all_users;
DELIMITER $$
CREATE PROCEDURE get_all_users()
BEGIN
    SELECT id, name, email, country 
    FROM users 
    ORDER BY id ASC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS update_user;
DELIMITER $$
CREATE PROCEDURE update_user(
    IN user_id INT,
    IN user_name VARCHAR(120),
    IN user_email VARCHAR(220),
    IN user_country VARCHAR(120)
)
BEGIN
    UPDATE users
    SET name = user_name,
        email = user_email,
        country = user_country
    WHERE id = user_id;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS delete_user;
DELIMITER $$
CREATE PROCEDURE delete_user(IN user_id INT)
BEGIN
    DELETE FROM users 
    WHERE id = user_id;
END$$
DELIMITER ;

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
    IN user_name VARCHAR(120),
    IN user_email VARCHAR(220),
    IN user_country VARCHAR(120)
)
BEGIN
    INSERT INTO users(name, email, country)
    VALUES(user_name, user_email, user_country);
END$$
DELIMITER ;
