INSERT INTO categories (name, parent_id) VALUES
    ('Electronics', NULL),
    ('Books', NULL),
    ('Clothing', NULL);

INSERT INTO categories (name, parent_id)
SELECT 'Laptops', id FROM categories WHERE name = 'Electronics';

INSERT INTO categories (name, parent_id)
SELECT 'Phones', id FROM categories WHERE name = 'Electronics';
