USE demo;

DROP PROCEDURE IF EXISTS get_all_users;
DROP PROCEDURE IF EXISTS update_user;
DROP PROCEDURE IF EXISTS delete_user;
DROP PROCEDURE IF EXISTS get_user_by_id;
DROP PROCEDURE IF EXISTS insert_user;

DELIMITER $$
CREATE PROCEDURE get_all_users()
BEGIN
    SELECT id, name, email, country 
    FROM users 
    ORDER BY id ASC;
END$$
DELIMITER ;

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

DELIMITER $$
CREATE PROCEDURE delete_user(IN user_id INT)
BEGIN
    DELETE FROM users 
    WHERE id = user_id;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE get_user_by_id(IN user_id INT)
BEGIN
    SELECT users.name, users.email, users.country
    FROM users
    WHERE users.id = user_id;
END$$
DELIMITER ;

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
