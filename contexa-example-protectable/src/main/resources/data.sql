INSERT INTO orders (customer_id, product_name, amount, created_at)
SELECT 1, 'Laptop', 1299.99, NOW()
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE product_name = 'Laptop' AND customer_id = 1);

INSERT INTO orders (customer_id, product_name, amount, created_at)
SELECT 1, 'Mouse', 29.99, NOW()
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE product_name = 'Mouse' AND customer_id = 1);

INSERT INTO orders (customer_id, product_name, amount, created_at)
SELECT 2, 'Keyboard', 89.99, NOW()
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE product_name = 'Keyboard' AND customer_id = 2);

INSERT INTO orders (customer_id, product_name, amount, created_at)
SELECT 2, 'Monitor', 449.99, NOW()
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE product_name = 'Monitor' AND customer_id = 2);

INSERT INTO orders (customer_id, product_name, amount, created_at)
SELECT 3, 'Headphones', 199.99, NOW()
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE product_name = 'Headphones' AND customer_id = 3);
