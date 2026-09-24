-- Users Table
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) DEFAULT 'USER',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tasks Table
CREATE TABLE task (
                      id SERIAL PRIMARY KEY,
                      user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      status VARCHAR(50) DEFAULT 'TODO',
                      priority VARCHAR(50) DEFAULT 'MEDIUM',
                      deadline DATE,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups
CREATE INDEX idx_task_user_id ON task(user_id);
CREATE INDEX idx_task_status ON task(status);
CREATE UNIQUE INDEX idx_user_email_deadline_title ON task(user_id, title, deadline);