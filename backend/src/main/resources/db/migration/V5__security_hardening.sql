-- Biztonsági megerősítés

-- Tokenverzió: jelszócsere, letiltás vagy kijelentkezés után a régi tokenek azonnal érvénytelenek
ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD CONSTRAINT ck_users_token_version CHECK (token_version >= 0);

-- A kijelentkezett tokenek azonosítója (jti); a lejárt sorokat az ütemező óránként törli
CREATE TABLE revoked_tokens (
    jti        VARCHAR(36) PRIMARY KEY,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_revoked_tokens_expires_at ON revoked_tokens (expires_at);

-- A kártyára a teljes név kerül (a vezeték- és a keresztnév legfeljebb 50-50 karakter)
ALTER TABLE cards ALTER COLUMN holder_name TYPE VARCHAR(150);

-- Havonta egy kamatjóváírás: az index akkor is véd, ha az ütemező és az admin egyszerre indítaná
CREATE UNIQUE INDEX uk_savings_tx_interest_once_per_month
    ON savings_transactions (savings_account_id, (date_trunc('month', created_at AT TIME ZONE 'Europe/Budapest')))
    WHERE type = 'INTEREST';

-- Kamatalap: az időszak legalacsonyabb egyenlege (tétel nélküli hónapban az időszak eleji egyenleg)
CREATE FUNCTION savings_min_balance(p_savings_id BIGINT, p_from TIMESTAMPTZ, p_to TIMESTAMPTZ)
RETURNS NUMERIC(18,2) AS $$
    SELECT LEAST(
        COALESCE((SELECT st.balance_after
                  FROM savings_transactions st
                  WHERE st.savings_account_id = p_savings_id
                    AND st.created_at < p_from
                  ORDER BY st.created_at DESC, st.id DESC
                  LIMIT 1), 0),
        (SELECT MIN(st.balance_after)
         FROM savings_transactions st
         WHERE st.savings_account_id = p_savings_id
           AND st.created_at >= p_from
           AND st.created_at < p_to)
    );
$$ LANGUAGE sql STABLE;

-- Teljesítési nap (1–31): a 31-i megbízás februárban a hó végén, márciusban újra 31-én teljesül
ALTER TABLE standing_orders ADD COLUMN execution_day INTEGER;
UPDATE standing_orders SET execution_day = EXTRACT(DAY FROM next_execution_date);
ALTER TABLE standing_orders ALTER COLUMN execution_day SET NOT NULL;
ALTER TABLE standing_orders ADD CONSTRAINT ck_standing_orders_execution_day CHECK (execution_day BETWEEN 1 AND 31);
