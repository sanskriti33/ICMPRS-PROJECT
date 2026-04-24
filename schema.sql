CREATE DATABASE IF NOT EXISTS complaintdb;
USE complaintdb;

CREATE TABLE IF NOT EXISTS complaints (
    id INT PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    priority INT NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    department VARCHAR(80) NOT NULL,
    status VARCHAR(50) NOT NULL,
    high_priority_flag INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
