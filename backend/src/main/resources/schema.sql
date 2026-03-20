CREATE DATABASE IF NOT EXISTS netdisk DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE netdisk;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    storage_used BIGINT DEFAULT 0,
    storage_quota BIGINT DEFAULT 10737418240,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文件表
CREATE TABLE IF NOT EXISTS files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT 0,
    content_type VARCHAR(100),
    storage_path VARCHAR(500) NOT NULL,
    file_hash VARCHAR(64),
    owner_id BIGINT NOT NULL,
    parent_folder_id BIGINT DEFAULT NULL,
    is_folder TINYINT DEFAULT 0,
    is_favorite TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    deleted TINYINT DEFAULT 0,
    INDEX idx_owner (owner_id),
    INDEX idx_parent (parent_folder_id),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分享表
CREATE TABLE IF NOT EXISTS shares (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    share_type TINYINT DEFAULT 1,
    password_hash VARCHAR(255) DEFAULT NULL,
    expires_at DATETIME DEFAULT NULL,
    max_downloads INT DEFAULT NULL,
    download_count INT DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (token),
    INDEX idx_file_id (file_id),
    FOREIGN KEY (file_id) REFERENCES files(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
