CREATE TABLE user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    role TEXT NOT NULL
);

CREATE TABLE books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    genre TEXT,
    owner INTEGER,
    price REAL,
    quantity INTEGER DEFAULT 1,
    FOREIGN KEY(owner) REFERENCES user(id) ON DELETE SET NULL
);

CREATE TABLE requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book INTEGER NOT NULL,
    requester INTEGER NOT NULL,
    status TEXT NOT NULL,
    FOREIGN KEY(book) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY(requester) REFERENCES user(id) ON DELETE CASCADE
);

SELECT u.id, b.title, u.username
            FROM requests r
            INNER JOIN books b ON r.book = b.id
            INNER JOIN user u ON u.id = r.requester
            WHERE b.owner = ? AND r.status = 'accepted'
