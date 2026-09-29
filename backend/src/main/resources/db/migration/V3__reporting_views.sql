-- =============================================================================
-- Riport nézet a statisztikákhoz
-- =============================================================================

-- Havi bevétel és kiadás számlánként, budapesti idő szerinti hónapokra bontva.
-- A FILTER záradékkal egyetlen olvasással készül el mindkét összeg.
CREATE VIEW v_account_monthly_summary AS
SELECT t.account_id,
       date_trunc('month', t.created_at AT TIME ZONE 'Europe/Budapest')::date AS month,
       COALESCE(SUM(t.amount) FILTER (WHERE t.type IN ('INCOME', 'TRANSFER_IN')), 0)   AS income,
       COALESCE(SUM(t.amount) FILTER (WHERE t.type IN ('OUTCOME', 'TRANSFER_OUT')), 0) AS outcome,
       COUNT(*) AS transaction_count
FROM transactions t
GROUP BY 1, 2;
