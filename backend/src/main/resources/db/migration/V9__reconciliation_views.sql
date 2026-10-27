-- Főkönyvi egyeztetés (reconciliation)

-- A tárolt és a tételekből számolt egyenleg; eltérés csak hibás kód vagy kézi SQL után lehet
CREATE VIEW v_account_reconciliation AS
SELECT a.id                AS account_id,
       a.user_id,
       a.account_number,
       a.name,
       a.currency,
       a.status,
       a.balance           AS stored_balance,
       COALESCE(SUM(CASE WHEN t.type IN ('INCOME', 'TRANSFER_IN') THEN t.amount ELSE -t.amount END), 0) AS ledger_balance,
       a.balance - COALESCE(SUM(CASE WHEN t.type IN ('INCOME', 'TRANSFER_IN') THEN t.amount ELSE -t.amount END), 0) AS difference,
       COUNT(t.id)         AS transaction_count
FROM accounts a
LEFT JOIN transactions t ON t.account_id = a.id
GROUP BY a.id, a.user_id, a.account_number, a.name, a.currency, a.status, a.balance;

-- Ugyanez a megtakarításokra, az utolsó mozgás utáni egyenleggel (balance_after) együtt
CREATE VIEW v_savings_reconciliation AS
SELECT s.id                AS savings_account_id,
       s.user_id,
       s.name,
       s.currency,
       s.status,
       s.balance           AS stored_balance,
       COALESCE(SUM(CASE WHEN st.type = 'WITHDRAWAL' THEN -st.amount ELSE st.amount END), 0) AS ledger_balance,
       s.balance - COALESCE(SUM(CASE WHEN st.type = 'WITHDRAWAL' THEN -st.amount ELSE st.amount END), 0) AS difference,
       (SELECT last_tx.balance_after
        FROM savings_transactions last_tx
        WHERE last_tx.savings_account_id = s.id
        ORDER BY last_tx.created_at DESC, last_tx.id DESC
        LIMIT 1)           AS last_balance_after,
       COUNT(st.id)        AS transaction_count
FROM savings_accounts s
LEFT JOIN savings_transactions st ON st.savings_account_id = s.id
GROUP BY s.id, s.user_id, s.name, s.currency, s.status, s.balance;
