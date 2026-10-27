-- Főkönyv: párosított tételek, sztornó, devizanem-ellenőrzés, pénzforgalmi szűrő

-- Belső utalásnál és devizaváltásnál a jóváírás a terhelésre mutat (a sztornó mindkettőt kezeli)
ALTER TABLE transactions ADD COLUMN related_transaction_id BIGINT REFERENCES transactions (id);
CREATE INDEX idx_transactions_related ON transactions (related_transaction_id) WHERE related_transaction_id IS NOT NULL;

-- Sztornó: ellentétes, az eredetire mutató tétel; egy tétel csak egyszer sztornózható
ALTER TABLE transactions ADD COLUMN reversal_of_id BIGINT REFERENCES transactions (id);
ALTER TABLE transactions ADD CONSTRAINT uk_transactions_reversal_of UNIQUE (reversal_of_id);
ALTER TABLE transactions ADD CONSTRAINT ck_transactions_reversal_not_self CHECK (reversal_of_id IS NULL OR reversal_of_id <> id);

-- A tétel devizaneme mindig a számláé, közvetlen SQL-lel sem könyvelhető más devizában
CREATE FUNCTION check_transaction_currency() RETURNS trigger AS $$
BEGIN
    IF NEW.currency IS DISTINCT FROM (SELECT a.currency FROM accounts a WHERE a.id = NEW.account_id) THEN
        RAISE EXCEPTION 'A tétel devizaneme (%) eltér a számláétól (számla: %)', NEW.currency, NEW.account_id
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_transactions_currency
    BEFORE INSERT ON transactions
    FOR EACH ROW EXECUTE FUNCTION check_transaction_currency();

-- Az egyenlegtörténet is csak bővíthető (a tranzakciókhoz hasonlóan)
CREATE TRIGGER trg_balance_history_immutable
    BEFORE UPDATE OR DELETE ON balance_history
    FOR EACH ROW EXECUTE FUNCTION forbid_ledger_change();

-- Pénzforgalom-e a tétel: a sztornópár, a saját számlák közti mozgás és a megtakarítás nem az
CREATE FUNCTION is_cashflow(p_transaction_id BIGINT) RETURNS BOOLEAN AS $$
    SELECT NOT (
        t.reversal_of_id IS NOT NULL
        OR EXISTS (SELECT 1 FROM transactions r WHERE r.reversal_of_id = t.id)
        OR (COALESCE(t.partner_name, '') = 'FineX megtakarítás' AND t.card_id IS NULL AND t.type IN ('INCOME', 'OUTCOME'))
        OR (t.type IN ('TRANSFER_IN', 'TRANSFER_OUT') AND EXISTS (
                SELECT 1
                FROM accounts own
                JOIN accounts other ON other.user_id = own.user_id
                WHERE own.id = t.account_id
                  AND other.account_number = CASE WHEN t.type = 'TRANSFER_OUT' THEN t.to_account ELSE t.from_account END))
    )
    FROM transactions t
    WHERE t.id = p_transaction_id;
$$ LANGUAGE sql STABLE;

-- A havi összesítő csak a valódi pénzforgalmat számolja (a tételszám az összes tételé)
CREATE OR REPLACE VIEW v_account_monthly_summary AS
SELECT t.account_id,
       date_trunc('month', t.created_at AT TIME ZONE 'Europe/Budapest')::date AS month,
       COALESCE(SUM(t.amount) FILTER (WHERE t.type IN ('INCOME', 'TRANSFER_IN') AND is_cashflow(t.id)), 0)   AS income,
       COALESCE(SUM(t.amount) FILTER (WHERE t.type IN ('OUTCOME', 'TRANSFER_OUT') AND is_cashflow(t.id)), 0) AS outcome,
       COUNT(*) AS transaction_count
FROM transactions t
GROUP BY 1, 2;
