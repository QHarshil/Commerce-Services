-- Commerce Services Database Schema
-- Optimized for high-throughput e-commerce operations

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Products table with inventory tracking
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory table for stock management with optimistic locking
CREATE TABLE inventory (
    product_id UUID PRIMARY KEY REFERENCES products(id),
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table with status tracking
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items for detailed order tracking
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL
);

-- Payments table for transaction tracking
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Outbox pattern for reliable event publishing
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 1
);

-- Saga orchestration for distributed transactions
CREATE TABLE saga_instances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    saga_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'STARTED',
    current_step VARCHAR(100),
    saga_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Idempotency keys for safe retries
CREATE TABLE idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    response_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- Performance indexes
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_outbox_events_processed ON outbox_events(processed_at) WHERE processed_at IS NULL;
CREATE INDEX idx_saga_instances_status ON saga_instances(status);
CREATE INDEX idx_idempotency_keys_expires ON idempotency_keys(expires_at);

-- Sample data for demonstration (use generated UUIDs; key by SKU)
INSERT INTO products (id, sku, name, description, price, category) VALUES
  (uuid_generate_v4(),'LAPTOP-001','Gaming Laptop Pro','High-performance gaming laptop with RTX 4080',1999.99,'Electronics'),
  (uuid_generate_v4(),'PHONE-001','Smartphone X','Latest flagship smartphone with 5G',899.99,'Electronics'),
  (uuid_generate_v4(),'BOOK-001','Clean Code','A handbook of agile software craftsmanship',42.99,'Books'),
  (uuid_generate_v4(),'SHIRT-001','Cotton T-Shirt','Comfortable cotton t-shirt in various colors',24.99,'Clothing'),
  (uuid_generate_v4(),'COFFEE-001','Premium Coffee Beans','Single-origin arabica coffee beans',18.99,'Food'),
  (uuid_generate_v4(),'MOUSE-001','Wireless Gaming Mouse','High-precision wireless gaming mouse',79.99,'Electronics'),
  (uuid_generate_v4(),'KEYBOARD-001','Mechanical Keyboard','RGB mechanical keyboard with blue switches',149.99,'Electronics'),
  (uuid_generate_v4(),'HEADPHONES-001','Noise-Canceling Headphones','Premium wireless noise-canceling headphones',299.99,'Electronics')
ON CONFLICT (sku) DO NOTHING;

-- Inventory quantities (resolve product_id via SKU)
INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 15, 2 FROM products p WHERE p.sku = 'LAPTOP-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 32, 5 FROM products p WHERE p.sku = 'PHONE-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 8, 1 FROM products p WHERE p.sku = 'BOOK-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 45, 3 FROM products p WHERE p.sku = 'SHIRT-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 23, 0 FROM products p WHERE p.sku = 'COFFEE-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 18, 2 FROM products p WHERE p.sku = 'MOUSE-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 12, 1 FROM products p WHERE p.sku = 'KEYBOARD-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT p.id, 25, 4 FROM products p WHERE p.sku = 'HEADPHONES-001'
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, reserved_quantity = EXCLUDED.reserved_quantity;

-- Trigger to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_inventory_updated_at BEFORE UPDATE ON inventory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_saga_instances_updated_at BEFORE UPDATE ON saga_instances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
