-- Insert roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Insert users with BCrypt hashed passwords
-- Password for both users: password
INSERT INTO users (username, password) VALUES 
('swathi', '$2a$10$MU0iRuS.arpokfD4d/wovefQGgYBjIDFfnF49jemdm4dzdFIz/WhS'), 
('admin',  '$2a$10$MU0iRuS.arpokfD4d/wovefQGgYBjIDFfnF49jemdm4dzdFIz/WhS');

-- Map users to roles
-- swathi -> ROLE_USER
-- admin  -> ROLE_USER + ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 1),
(2, 2);