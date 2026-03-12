INSERT INTO accounts (user_id, balance)
SELECT 1, 10000.00
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = 1);

INSERT INTO accounts (user_id, balance)
SELECT 2, 5000.00
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = 2);

INSERT INTO accounts (user_id, balance)
SELECT 3, 2500.00
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = 3);
