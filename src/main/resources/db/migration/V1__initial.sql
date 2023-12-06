CREATE TABLE items
(
    id                 UUID PRIMARY KEY,
    description        VARCHAR(500) NOT NULL,
    status             VARCHAR(50)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_date TIMESTAMP,
    due_date           TIMESTAMP    NOT NULL,
    completed_date     TIMESTAMP
);
