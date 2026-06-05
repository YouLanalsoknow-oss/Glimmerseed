CREATE DATABASE IF NOT EXISTS glimmerseed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE glimmerseed;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    appearance VARCHAR(255) NOT NULL,
    personality VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    last_interacted_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    role VARCHAR(50) NOT NULL,
    timestamp DATETIME NOT NULL,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

INSERT INTO users (email, password, username, created_at, updated_at) VALUES 
('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '管理员', NOW(), NOW());
