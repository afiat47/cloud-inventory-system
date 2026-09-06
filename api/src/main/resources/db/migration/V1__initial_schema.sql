CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(256) NOT NULL,
    full_name   VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120)  NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(18,2) NOT NULL,
    quantity    INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_name ON products (name);

CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    total       NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT
);
CREATE INDEX idx_orders_user_id ON orders (user_id);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT        NOT NULL,
    product_id  BIGINT        NOT NULL,
    quantity    INTEGER       NOT NULL,
    unit_price  NUMERIC(18,2) NOT NULL,
    CONSTRAINT fk_items_order   FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_items_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT uq_order_product UNIQUE (order_id, product_id)
);
