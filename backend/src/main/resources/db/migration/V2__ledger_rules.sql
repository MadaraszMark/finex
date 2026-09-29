-- =============================================================================
-- Könyvelési szabályok és kivonat az adatbázis szintjén
-- =============================================================================

-- 1) A könyvelt tételek nem módosíthatók és nem törölhetők (append-only főkönyv).
--    Hibás könyvelést új, ellentétes irányú tétellel lehet javítani, az eredeti
--    átírásával nem. A Java-kód is betartja ezt, az adatbázis pedig akkor is
--    garantálja, ha valaki közvetlenül SQL-lel próbálkozna.
CREATE FUNCTION forbid_ledger_change() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'A könyvelt tétel nem módosítható és nem törölhető (tábla: %, id: %)', TG_TABLE_NAME, OLD.id
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_transactions_immutable
    BEFORE UPDATE OR DELETE ON transactions
    FOR EACH ROW EXECUTE FUNCTION forbid_ledger_change();

CREATE TRIGGER trg_savings_transactions_immutable
    BEFORE UPDATE OR DELETE ON savings_transactions
    FOR EACH ROW EXECUTE FUNCTION forbid_ledger_change();

-- 2) Egy számla egyenlege egy adott időpontban: a mostani egyenlegből
--    "visszatekerjük" az időpont óta könyvelt tételeket.
CREATE FUNCTION account_balance_at(p_account_id BIGINT, p_at TIMESTAMPTZ)
RETURNS NUMERIC(18,2) AS $$
    SELECT a.balance
           - COALESCE(SUM(CASE WHEN t.type IN ('INCOME', 'TRANSFER_IN') THEN t.amount ELSE -t.amount END), 0)
    FROM accounts a
    LEFT JOIN transactions t ON t.account_id = a.id AND t.created_at >= p_at
    WHERE a.id = p_account_id
    GROUP BY a.id, a.balance;
$$ LANGUAGE sql STABLE;

-- 3) Számlakivonat: az időszak tételei futó egyenleggel. A futó egyenleget
--    ablakfüggvény (SUM ... OVER) számolja a nyitóegyenlegtől indulva.
CREATE FUNCTION account_statement(p_account_id BIGINT, p_from TIMESTAMPTZ, p_to TIMESTAMPTZ)
RETURNS TABLE (
    transaction_id  BIGINT,
    created_at      TIMESTAMPTZ,
    type            VARCHAR,
    partner_name    VARCHAR,
    message         VARCHAR,
    amount          NUMERIC,
    signed_amount   NUMERIC,
    running_balance NUMERIC
) AS $$
    WITH opening AS (
        SELECT account_balance_at(p_account_id, p_from) AS balance
    )
    SELECT t.id,
           t.created_at,
           t.type,
           t.partner_name,
           t.message,
           t.amount,
           s.signed_amount,
           o.balance + SUM(s.signed_amount) OVER (ORDER BY t.created_at, t.id)
    FROM transactions t
    CROSS JOIN opening o
    CROSS JOIN LATERAL (
        SELECT CASE WHEN t.type IN ('INCOME', 'TRANSFER_IN') THEN t.amount ELSE -t.amount END AS signed_amount
    ) s
    WHERE t.account_id = p_account_id
      AND t.created_at >= p_from
      AND t.created_at < p_to
    ORDER BY t.created_at, t.id;
$$ LANGUAGE sql STABLE;
