
-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    color TEXT
);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_key TEXT NOT NULL,
    question TEXT NOT NULL,
    prompt TEXT,
    tips TEXT,
    FOREIGN KEY (category_key) REFERENCES categories(key)
);

-- Chat history table
CREATE TABLE IF NOT EXISTS chat_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    sender TEXT NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- User progress table
CREATE TABLE IF NOT EXISTS user_stats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_key TEXT,
    questions_viewed INTEGER DEFAULT 0,
    last_accessed DATETIME,
    rating INTEGER
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_category_key ON categories(key);
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category_key);
CREATE INDEX IF NOT EXISTS idx_chat_question ON chat_history(question_id);
CREATE INDEX IF NOT EXISTS idx_chat_timestamp ON chat_history(timestamp);
CREATE INDEX IF NOT EXISTS idx_user_stats_category ON user_stats(category_key);
