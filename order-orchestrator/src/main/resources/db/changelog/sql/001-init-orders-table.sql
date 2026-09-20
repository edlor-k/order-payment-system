-- changeset init-orders-:001

CREATE TABLE IF NOT EXISTS orders
(
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    address             TEXT,
    client_estimate     DECIMAL,
    final_amount        DECIMAL,
    authorized_amount   DECIMAL,
    captured_amount     DECIMAL,
    payment_status      SMALLINT,
    failure_reason      TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL
);