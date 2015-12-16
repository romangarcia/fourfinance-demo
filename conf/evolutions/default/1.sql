# Loans schema

# --- !Ups

CREATE TABLE loans (
    id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    amount DECIMAL(16,4) NOT NULL,
    term_days INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    client_ip VARCHAR(16) NOT NULL,
    approved BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE loans;