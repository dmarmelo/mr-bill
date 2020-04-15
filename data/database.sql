CREATE TABLE user (
    id INTEGER PRIMARY KEY,
    name TEXT,
    username TEXT,
    password TEXT,
    apikey TEXT
);

CREATE TABLE customer (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    name TEXT,
    email TEXT
);

CREATE TABLE invoice (
    id INTEGER PRIMARY KEY,
    customer_id INTEGER,
    amount REAL,
    date TEXT,
    complete INTEGER
);

INSERT INTO user (name, username, password, apikey) VALUES ("Bart Simpson", "bart", "1234", "xg5idk5");
INSERT INTO user (name, username, password, apikey) VALUES ("Homer Simpson", "homer", "1234", "fl2hsb4");

INSERT INTO customer (user_id, name, email) VALUES (1, "Lisa Simpson", "lisa@simpsons.org");
INSERT INTO customer (user_id, name, email) VALUES (1, "Maggie Simpson", "maggie@simpsons.org");
INSERT INTO customer (user_id, name, email) VALUES (2, "Marge Simpson", "marge@simpsons.org");

INSERT INTO invoice (customer_id, amount, date, complete) VALUES (1, 12.49, "2020-02-19", 1);
INSERT INTO invoice (customer_id, amount, date, complete) VALUES (1, 29.99, "2020-02-20", 0);
INSERT INTO invoice (customer_id, amount, date, complete) VALUES (2, 9.99, "2020-02-21", 1);
INSERT INTO invoice (customer_id, amount, date, complete) VALUES (3, 19.99, "2020-02-21", 0);