CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE TABLE addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users (id),
    recipient_name  VARCHAR(200) NOT NULL,
    phone           VARCHAR(50)  NOT NULL,
    country         VARCHAR(100) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    street          VARCHAR(255) NOT NULL,
    postal_code     VARCHAR(30)  NOT NULL,
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    parent_id   BIGINT REFERENCES categories (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
);

CREATE INDEX idx_categories_parent_id ON categories (parent_id);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT         NOT NULL REFERENCES categories (id),
    name            VARCHAR(255)   NOT NULL,
    description     TEXT,
    sku             VARCHAR(100)   NOT NULL,
    price           NUMERIC(19, 2) NOT NULL,
    stock_quantity  INT            NOT NULL DEFAULT 0,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_active ON products (active);
CREATE INDEX idx_products_name ON products (name);

CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_carts_user_id UNIQUE (user_id)
);

CREATE TABLE cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT      NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id  BIGINT      NOT NULL REFERENCES products (id),
    quantity    INT         NOT NULL,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);

CREATE TABLE orders (
    id                   BIGSERIAL PRIMARY KEY,
    order_number         VARCHAR(40)    NOT NULL,
    user_id              BIGINT         NOT NULL REFERENCES users (id),
    status               VARCHAR(20)    NOT NULL,
    total_amount         NUMERIC(19, 2) NOT NULL,
    shipping_recipient   VARCHAR(200)   NOT NULL,
    shipping_phone       VARCHAR(50)    NOT NULL,
    shipping_country     VARCHAR(100)   NOT NULL,
    shipping_city        VARCHAR(100)   NOT NULL,
    shipping_street      VARCHAR(255)   NOT NULL,
    shipping_postal_code  VARCHAR(30)    NOT NULL,
    paid_at              TIMESTAMPTZ,
    shipped_at           TIMESTAMPTZ,
    delivered_at         TIMESTAMPTZ,
    cancelled_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT ck_orders_total CHECK (total_amount >= 0)
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);

CREATE TABLE order_items (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id              BIGINT         NOT NULL REFERENCES products (id),
    product_name_snapshot   VARCHAR(255)   NOT NULL,
    unit_price_at_purchase  NUMERIC(19, 2) NOT NULL,
    quantity                INT            NOT NULL,
    line_total              NUMERIC(19, 2) NOT NULL,
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price_at_purchase >= 0),
    CONSTRAINT ck_order_items_line_total CHECK (line_total >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);

CREATE TABLE order_status_history (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT      NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    from_status  VARCHAR(20),
    to_status    VARCHAR(20) NOT NULL,
    changed_by   BIGINT      REFERENCES users (id),
    changed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    comment      VARCHAR(500),
    CONSTRAINT ck_osh_from_status CHECK (from_status IS NULL OR from_status IN ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT ck_osh_to_status CHECK (to_status IN ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

CREATE INDEX idx_osh_order_id ON order_status_history (order_id);
CREATE INDEX idx_osh_changed_at ON order_status_history (changed_at);
