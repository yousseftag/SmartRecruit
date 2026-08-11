-- V2: Add username to app_user table
ALTER TABLE app_user ADD COLUMN username VARCHAR(255) UNIQUE NOT NULL;
