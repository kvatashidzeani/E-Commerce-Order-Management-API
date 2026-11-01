-- Extra categories (idempotent)
INSERT INTO categories (name, parent_id)
SELECT v.name, NULL
FROM (VALUES
    ('Food'),
    ('Home'),
    ('Sports')
) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE lower(c.name) = lower(v.name));

-- Demo catalog: at least 3 products per category. LAP-LAST-1 has stock = 1 for concurrency demos.
INSERT INTO products (category_id, name, description, sku, price, stock_quantity, active)
SELECT c.id, v.name, v.description, v.sku, v.price, v.stock_quantity, TRUE
FROM (VALUES
    -- Laptops
    ('Laptops', 'ThinkPad X1 Carbon', '14-inch business ultrabook with all-day battery', 'LAP-X1C-001', 1499.00, 25),
    ('Laptops', 'MacBook Air M3', 'Featherweight laptop with Apple M3 chip', 'LAP-MBA-M3', 1199.00, 18),
    ('Laptops', 'Dell XPS 13', 'Premium Windows ultrabook with infinity edge display', 'LAP-XPS-13', 1299.00, 12),
    ('Laptops', 'ASUS ZenBook 14', 'OLED productivity laptop in a slim chassis', 'LAP-ZEN-14', 999.00, 20),
    ('Laptops', 'HP EliteBook 840', 'Enterprise laptop with hardware security', 'LAP-ELB-840', 1149.00, 15),
    ('Laptops', 'Lenovo IdeaPad Slim', 'Affordable everyday laptop for students', 'LAP-IDEA-SL', 649.00, 40),
    ('Laptops', 'Acer Swift Go', 'Thin aluminum laptop for travel', 'LAP-SWF-GO', 799.00, 22),
    ('Laptops', 'Last Unit Demo Laptop', 'Concurrency demo product with stock=1', 'LAP-LAST-1', 899.00, 1),
    -- Phones
    ('Phones', 'iPhone 16', 'Apple flagship smartphone with A18 chip', 'PHN-IP16-128', 899.00, 30),
    ('Phones', 'Samsung Galaxy S25', 'Android flagship with bright AMOLED display', 'PHN-SGS25-256', 949.00, 28),
    ('Phones', 'Google Pixel 9', 'Clean Android experience and excellent camera', 'PHN-PX9-128', 799.00, 24),
    ('Phones', 'OnePlus 13', 'Flagship killer with ultra-fast charging', 'PHN-OP13-256', 749.00, 20),
    ('Phones', 'Xiaomi 15', 'High-spec phone at a sharp price', 'PHN-MI15-256', 699.00, 35),
    ('Phones', 'Nothing Phone 3', 'Unique Glyph interface and clean software', 'PHN-NTH-3', 599.00, 16),
    -- Electronics
    ('Electronics', 'Sony WH-1000XM5', 'Industry-leading noise cancelling headphones', 'ELC-SONY-XM5', 349.00, 45),
    ('Electronics', 'Apple AirPods Pro 2', 'ANC earbuds with spatial audio', 'ELC-APP-PRO2', 249.00, 60),
    ('Electronics', 'Logitech MX Master 3S', 'Ergonomic productivity mouse', 'ELC-LOG-MX3S', 99.00, 80),
    ('Electronics', 'Keychron Q1 Pro', 'Wireless mechanical keyboard', 'ELC-KEY-Q1P', 179.00, 35),
    ('Electronics', 'Samsung 27" Monitor', '1440p IPS monitor for work and play', 'ELC-SAM-27Q', 329.00, 22),
    ('Electronics', 'Anker 737 Power Bank', 'High capacity USB-C power bank', 'ELC-ANK-737', 129.00, 50),
    -- Books
    ('Books', 'Clean Code', 'Timeless guide to writing maintainable software — Robert C. Martin', 'BOK-CLEAN-CODE', 39.99, 100),
    ('Books', 'Designing Data-Intensive Apps', 'Modern data systems explained — Martin Kleppmann', 'BOK-DDIA', 49.99, 70),
    ('Books', 'Effective Java', 'Best practices for the Java platform — Joshua Bloch', 'BOK-EFF-JAVA', 44.99, 55),
    ('Books', 'Domain-Driven Design', 'Tackling complexity in software — Eric Evans', 'BOK-DDD', 54.99, 40),
    ('Books', 'The Pragmatic Programmer', 'Classic craftsmanship advice — Hunt & Thomas', 'BOK-PRAG-PROG', 42.99, 65),
    ('Books', 'Refactoring', 'Improving the design of existing code — Martin Fowler', 'BOK-REFACTOR', 47.99, 48),
    -- Clothing
    ('Clothing', 'Merino Crew Neck', 'Soft everyday sweater in merino wool', 'CLT-MER-CREW', 79.00, 60),
    ('Clothing', 'Chino Pants', 'Slim fit chino for smart-casual looks', 'CLT-CHINO-32', 59.00, 70),
    ('Clothing', 'Waterproof Jacket', 'Light rain shell for city weather', 'CLT-RAIN-JKT', 119.00, 35),
    ('Clothing', 'Running Shoes', 'Cushioned road runners', 'CLT-RUN-42', 129.00, 45),
    ('Clothing', 'Cotton T-Shirt 3-Pack', 'Everyday basics in soft cotton', 'CLT-TEE-3PK', 29.00, 120),
    ('Clothing', 'Linen Shirt', 'Breathable summer shirt', 'CLT-LIN-SH', 69.00, 40),
    -- Food
    ('Food', 'Organic Olive Oil 500ml', 'Cold-pressed extra virgin olive oil', 'FOOD-OIL-500', 18.50, 80),
    ('Food', 'Arabica Coffee Beans 1kg', 'Medium roast specialty coffee', 'FOOD-COF-1KG', 24.90, 55),
    ('Food', 'Dark Chocolate 85%', 'Single-origin cacao bar', 'FOOD-CHOC-85', 6.50, 120),
    ('Food', 'Artisan Pasta Pack', 'Bronze-cut durum wheat pasta', 'FOOD-PASTA-1', 4.90, 90),
    ('Food', 'Green Tea Sampler', 'Six regional green teas', 'FOOD-TEA-6', 16.00, 70),
    -- Home
    ('Home', 'Ceramic Pour-Over Set', 'Hand-glazed dripper and mug', 'HOME-POUR-1', 42.00, 35),
    ('Home', 'Linen Bedding Set', 'Breathable queen size linen set', 'HOME-LIN-QN', 129.00, 20),
    ('Home', 'Oak Desk Lamp', 'Warm LED lamp with oak base', 'HOME-LAMP-OK', 69.00, 28),
    ('Home', 'Scented Candle Trio', 'Cedar, fig and sea salt', 'HOME-CND-3', 34.00, 45),
    -- Sports
    ('Sports', 'Yoga Mat Pro', 'Non-slip 5mm mat with carry strap', 'SPT-YOGA-5', 39.00, 50),
    ('Sports', 'Adjustable Dumbbells', 'Pair adjustable from 2–20 kg', 'SPT-DUMB-20', 189.00, 15),
    ('Sports', 'Insulated Water Bottle', '1L stainless steel bottle', 'SPT-BTL-1L', 28.00, 75),
    ('Sports', 'Resistance Band Set', 'Five levels with door anchor', 'SPT-BAND-5', 22.00, 60)
) AS v(category_name, name, description, sku, price, stock_quantity)
JOIN categories c ON lower(c.name) = lower(v.category_name)
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.sku = v.sku);
