ALTER TABLE products
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Category defaults (applied first, then SKU-specific overrides)
UPDATE products p
SET image_url = v.url
FROM (VALUES
    ('Laptops', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80'),
    ('Phones', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80'),
    ('Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80'),
    ('Books', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=80'),
    ('Clothing', 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=900&q=80'),
    ('Food', 'https://images.unsplash.com/photo-1498837164567-7900fe229858?auto=format&fit=crop&w=900&q=80'),
    ('Home', 'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=900&q=80'),
    ('Sports', 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=900&q=80')
) AS v(category_name, url)
JOIN categories c ON lower(c.name) = lower(v.category_name)
WHERE p.category_id = c.id
  AND (p.image_url IS NULL OR p.image_url = '');

-- Distinct photos per known SKU
UPDATE products AS p
SET image_url = v.url
FROM (VALUES
    ('LAP-X1C-001', 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=900&q=80'),
    ('LAP-MBA-M3', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=80'),
    ('LAP-XPS-13', 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=900&q=80'),
    ('LAP-ZEN-14', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80'),
    ('LAP-ELB-840', 'https://images.unsplash.com/photo-1525547719571-a2d4ac882e75?auto=format&fit=crop&w=900&q=80'),
    ('LAP-IDEA-SL', 'https://images.unsplash.com/photo-1484788984921-03950022c9ef?auto=format&fit=crop&w=900&q=80'),
    ('LAP-SWF-GO', 'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=900&q=80'),
    ('LAP-LAST-1', 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=900&q=80'),
    ('PHN-IP16-128', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=900&q=80'),
    ('PHN-SGS25-256', 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=900&q=80'),
    ('PHN-PX9-128', 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=80'),
    ('PHN-OP13-256', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80'),
    ('PHN-MI15-256', 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?auto=format&fit=crop&w=900&q=80'),
    ('PHN-NTH-3', 'https://images.unsplash.com/photo-1580910051074-3eb694886505?auto=format&fit=crop&w=900&q=80'),
    ('ELC-SONY-XM5', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80'),
    ('ELC-APP-PRO2', 'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=900&q=80'),
    ('ELC-LOG-MX3S', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=900&q=80'),
    ('ELC-KEY-Q1P', 'https://images.unsplash.com/photo-1511467687898-71882bbf8f2f?auto=format&fit=crop&w=900&q=80'),
    ('ELC-SAM-27Q', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=900&q=80'),
    ('ELC-ANK-737', 'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=900&q=80'),
    ('BOK-CLEAN-CODE', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=900&q=80'),
    ('BOK-DDIA', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=900&q=80'),
    ('BOK-EFF-JAVA', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=80'),
    ('BOK-DDD', 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=900&q=80'),
    ('BOK-PRAG-PROG', 'https://images.unsplash.com/photo-1481627834876-b7833e8f6540?auto=format&fit=crop&w=900&q=80'),
    ('BOK-REFACTOR', 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=900&q=80'),
    ('CLT-MER-CREW', 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?auto=format&fit=crop&w=900&q=80'),
    ('CLT-CHINO-32', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=900&q=80'),
    ('CLT-RAIN-JKT', 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?auto=format&fit=crop&w=900&q=80'),
    ('CLT-RUN-42', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80'),
    ('CLT-TEE-3PK', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80'),
    ('CLT-LIN-SH', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=900&q=80'),
    ('FOOD-OIL-500', 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=900&q=80'),
    ('FOOD-COF-1KG', 'https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=900&q=80'),
    ('FOOD-CHOC-85', 'https://images.unsplash.com/photo-1511381939415-e44015466834?auto=format&fit=crop&w=900&q=80'),
    ('FOOD-PASTA-1', 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=900&q=80'),
    ('FOOD-TEA-6', 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&w=900&q=80'),
    ('HOME-POUR-1', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=80'),
    ('HOME-LIN-QN', 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=900&q=80'),
    ('HOME-LAMP-OK', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=80'),
    ('HOME-CND-3', 'https://images.unsplash.com/photo-1602607387330-95c5f0e8595b?auto=format&fit=crop&w=900&q=80'),
    ('SPT-YOGA-5', 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=900&q=80'),
    ('SPT-DUMB-20', 'https://images.unsplash.com/photo-1576678927484-cc907957088c?auto=format&fit=crop&w=900&q=80'),
    ('SPT-BTL-1L', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=80'),
    ('SPT-BAND-5', 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?auto=format&fit=crop&w=900&q=80')
) AS v(sku, url)
WHERE p.sku = v.sku;
