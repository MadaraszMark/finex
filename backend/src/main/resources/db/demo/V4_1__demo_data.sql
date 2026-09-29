-- =============================================================================
-- Demóadatok fejlesztéshez és bemutatóhoz – a tesztek NEM töltik be
-- (spring.flyway.locations = classpath:db/migration,classpath:db/demo)
--
-- Bejelentkezés:
--   anna@finex.hu  / Demo1234!   felhasználó, fél év tranzakciós előzménnyel
--   bence@finex.hu / Demo1234!   felhasználó (Anna ismerőse, egymásnak is utalnak)
--   admin@finex.hu / Admin1234!  adminisztrátor
--
-- Az egyenlegek nincsenek "beírva": a lenti blokk időrendben lekönyveli a
-- tételeket, és közben vezeti a futó egyenleget, ahogy az alkalmazás is tenné.
-- Az időpontok a migráció futásához képest relatívak (az elmúlt hat hónap).
-- =============================================================================

INSERT INTO users (first_name, last_name, email, phone, password_hash, role, status, created_at, updated_at) VALUES
    ('Anna', 'Kovács', 'anna@finex.hu', '+36301234567', '$2a$10$aPl/8UAL7uJmFf5b7FppH.0r4BCp.dm7bGiiiVYKz4MgPibVsUqJS', 'USER', 'ACTIVE', now() - INTERVAL '6 months', now() - INTERVAL '6 months'),
    ('Bence', 'Nagy', 'bence@finex.hu', '+36209876543', '$2a$10$aPl/8UAL7uJmFf5b7FppH.0r4BCp.dm7bGiiiVYKz4MgPibVsUqJS', 'USER', 'ACTIVE', now() - INTERVAL '6 months', now() - INTERVAL '6 months'),
    ('Admin', 'FineX', 'admin@finex.hu', NULL, '$2a$10$tSdsYSkR3AJqYzR9djUWVeb2l1V4NMSmpsDS4/pobbJVYvEGwhYFm', 'ADMIN', 'ACTIVE', now() - INTERVAL '6 months', now() - INTERVAL '6 months');

-- Az egyenleg 0-ról indul, a tételek könyvelése állítja be.
-- Anna euroszámlája a forintszámla után nyílt: az elsődleges számla (GET /accounts/me) a legrégebbi aktív folyószámla.
INSERT INTO accounts (user_id, name, account_number, account_type, balance, currency, status, created_at)
SELECT u.id, v.name, v.iban, 'CURRENT', 0, v.currency, 'ACTIVE', now() - v.opened_ago
FROM (VALUES
    ('anna@finex.hu', 'Fő számla', 'HU15117730161111101800000001', 'HUF', INTERVAL '6 months'),
    ('anna@finex.hu', 'Euró számla', 'HU85117730161111101800000002', 'EUR', INTERVAL '5 months 29 days'),
    ('bence@finex.hu', 'Fő számla', 'HU28104000950000521700000003', 'HUF', INTERVAL '6 months'),
    ('admin@finex.hu', 'Fő számla', 'HU50120100070000123400000004', 'HUF', INTERVAL '6 months')
) AS v(email, name, iban, currency, opened_ago)
JOIN users u ON u.email = v.email;

INSERT INTO cards (account_id, card_number, holder_name, expiry_date, status, daily_limit, online_payment_enabled, created_at)
SELECT a.id, v.card_number, v.holder_name, (date_trunc('month', now()) + INTERVAL '4 years' - INTERVAL '1 day')::date,
       'ACTIVE', v.daily_limit, TRUE, a.created_at
FROM (VALUES
    ('HU15117730161111101800000001', '4895127301601184', 'KOVÁCS ANNA', 300000),
    ('HU85117730161111101800000002', '4895127301602273', 'KOVÁCS ANNA', 1000),
    ('HU28104000950000521700000003', '4895121040095014', 'NAGY BENCE', 200000),
    ('HU50120100070000123400000004', '4895121201000779', 'ADMIN FINEX', 200000)
) AS v(iban, card_number, holder_name, daily_limit)
JOIN accounts a ON a.account_number = v.iban;

-- Az aktuális és az előző öt hónap első napja, budapesti idő szerint (m = 0 a legrégebbi)
CREATE TEMP TABLE demo_months ON COMMIT DROP AS
SELECT m,
       (date_trunc('month', now() AT TIME ZONE 'Europe/Budapest') - make_interval(months => 5 - m))
           AT TIME ZONE 'Europe/Budapest' AS month_start
FROM generate_series(0, 5) AS m;

CREATE TEMP TABLE demo_events (
    account_number VARCHAR(34)   NOT NULL,
    event_time     TIMESTAMPTZ   NOT NULL,
    type           VARCHAR(20)   NOT NULL,
    amount         NUMERIC(18,2) NOT NULL,
    partner_name   VARCHAR(150),
    message        VARCHAR(255),
    category       VARCHAR(100),
    with_card      BOOLEAN       NOT NULL DEFAULT FALSE,
    from_account   VARCHAR(34),
    to_account     VARCHAR(34)
) ON COMMIT DROP;

-- Anna havi tételei: every = 1 minden hónap, 2 páros, 3 páratlan hónap. Az összeg a hónaptól függően kicsit változik.
INSERT INTO demo_events (account_number, event_time, type, amount, partner_name, message, category, with_card, to_account)
SELECT 'HU15117730161111101800000001',
       mo.month_start + INTERVAL '1 day' * (t.day - 1) + t.at_time,
       t.type,
       t.base + ((mo.m * 7 + t.day * 3) % 5) * t.step,
       t.partner, t.message, t.category, t.with_card, t.to_account
FROM demo_months mo
CROSS JOIN (VALUES
    (2,  INTERVAL '07:40', 'OUTCOME',      8950,   0,    'BKK',                'Havi bérlet',            'Közlekedés',      TRUE,  NULL, 1),
    (3,  INTERVAL '18:20', 'OUTCOME',      9000,   1850, 'Tesco',              'Bevásárlás',             'Élelmiszer',      TRUE,  NULL, 1),
    (5,  INTERVAL '09:00', 'INCOME',       685000, 0,    'Netlab Kft.',        'Munkabér',               'Fizetés',         FALSE, NULL, 1),
    (6,  INTERVAL '00:30', 'TRANSFER_OUT', 220000, 0,    'Tóth Gábor',         'Albérlet',               'Lakhatás',        FALSE, 'HU66109180010000048900240017', 1),
    (7,  INTERVAL '09:30', 'OUTCOME',      30000,  0,    'FineX megtakarítás', 'Megtakarítás: Nyaralás', 'Megtakarítás',    FALSE, NULL, 1),
    (8,  INTERVAL '19:45', 'OUTCOME',      6500,   1400, 'Wolt',               'Vacsora rendelés',       'Étterem, kávézó', TRUE,  NULL, 1),
    (10, INTERVAL '18:05', 'OUTCOME',      8000,   1600, 'Lidl',               'Bevásárlás',             'Élelmiszer',      TRUE,  NULL, 1),
    (11, INTERVAL '11:00', 'TRANSFER_OUT', 14500,  900,  'MVM Next',           'Villanyszámla',          'Rezsi',           FALSE, 'HU39116000060000000012345678', 1),
    (15, INTERVAL '21:10', 'OUTCOME',      5400,   0,    'Cinema City',        'Mozijegy',               'Szórakozás',      TRUE,  NULL, 1),
    (17, INTERVAL '17:50', 'OUTCOME',      7000,   1300, 'Spar',               'Bevásárlás',             'Élelmiszer',      TRUE,  NULL, 1),
    (19, INTERVAL '16:30', 'OUTCOME',      14000,  4200, 'Decathlon',          'Sportfelszerelés',       'Vásárlás',        TRUE,  NULL, 3),
    (19, INTERVAL '16:30', 'OUTCOME',      12000,  3500, 'H&M',                'Ruházat',                'Vásárlás',        TRUE,  NULL, 2),
    (21, INTERVAL '13:00', 'OUTCOME',      9000,   2200, 'Bistro Budapest',    'Ebéd',                   'Étterem, kávézó', TRUE,  NULL, 1),
    (22, INTERVAL '10:00', 'OUTCOME',      4200,   800,  'Benu Gyógyszertár',  'Gyógyszertár',           'Egészség',        TRUE,  NULL, 2),
    (24, INTERVAL '18:40', 'OUTCOME',      8500,   1700, 'Aldi',               'Bevásárlás',             'Élelmiszer',      TRUE,  NULL, 1),
    (27, INTERVAL '08:00', 'OUTCOME',      1990,   0,    'Spotify',            'Előfizetés',             'Szórakozás',      TRUE,  NULL, 1),
    (27, INTERVAL '08:05', 'OUTCOME',      3990,   0,    'Netflix',            'Előfizetés',             'Szórakozás',      TRUE,  NULL, 1)
) AS t(day, at_time, type, base, step, partner, message, category, with_card, to_account, every)
WHERE (t.every = 1 OR (t.every = 2 AND mo.m % 2 = 0) OR (t.every = 3 AND mo.m % 2 = 1))
  AND mo.month_start + INTERVAL '1 day' * (t.day - 1) + t.at_time <= now();

-- Bence havi tételei
INSERT INTO demo_events (account_number, event_time, type, amount, partner_name, message, category, with_card, to_account)
SELECT 'HU28104000950000521700000003',
       mo.month_start + INTERVAL '1 day' * (t.day - 1) + t.at_time,
       t.type,
       t.base + ((mo.m * 5 + t.day * 7) % 5) * t.step,
       t.partner, t.message, t.category, t.with_card, t.to_account
FROM demo_months mo
CROSS JOIN (VALUES
    (2,  INTERVAL '07:55', 'OUTCOME',      8950,   0,    'BKK',               'Havi bérlet',   'Közlekedés',      TRUE,  NULL),
    (4,  INTERVAL '19:10', 'OUTCOME',      11000,  2100, 'Penny',             'Bevásárlás',    'Élelmiszer',      TRUE,  NULL),
    (10, INTERVAL '08:30', 'INCOME',       540000, 0,    'Grafit Studio Bt.', 'Munkabér',      'Fizetés',         FALSE, NULL),
    (13, INTERVAL '11:20', 'TRANSFER_OUT', 12800,  700,  'MVM Next',          'Villanyszámla', 'Rezsi',           FALSE, 'HU39116000060000000012345678'),
    (14, INTERVAL '18:30', 'OUTCOME',      9500,   1900, 'Tesco',             'Bevásárlás',    'Élelmiszer',      TRUE,  NULL),
    (16, INTERVAL '20:00', 'OUTCOME',      7800,   1500, 'Burger King',       'Vacsora',       'Étterem, kávézó', TRUE,  NULL)
) AS t(day, at_time, type, base, step, partner, message, category, with_card, to_account)
WHERE mo.month_start + INTERVAL '1 day' * (t.day - 1) + t.at_time <= now();

-- Egyszeri tételek: nyitó befizetések, a Vésztartalék indítása, euró számla mozgásai
INSERT INTO demo_events (account_number, event_time, type, amount, partner_name, message, category, with_card)
SELECT v.iban, mo.month_start + v.at_time, v.type, v.amount, v.partner, v.message, v.category, v.with_card
FROM demo_months mo
JOIN (VALUES
    (0, 'HU15117730161111101800000001', INTERVAL '10:00',          'INCOME',  250000, 'FineX fiók',         'Készpénzbefizetés',          NULL,           FALSE),
    (0, 'HU28104000950000521700000003', INTERVAL '10:30',          'INCOME',  180000, 'FineX fiók',         'Készpénzbefizetés',          NULL,           FALSE),
    (0, 'HU50120100070000123400000004', INTERVAL '11:00',          'INCOME',  100000, 'FineX fiók',         'Készpénzbefizetés',          NULL,           FALSE),
    (0, 'HU85117730161111101800000002', INTERVAL '11:30',          'INCOME',  1200,   'FineX fiók',         'Készpénzbefizetés',          NULL,           FALSE),
    (0, 'HU15117730161111101800000001', INTERVAL '8 days 10:00',   'OUTCOME', 150000, 'FineX megtakarítás', 'Megtakarítás: Vésztartalék', 'Megtakarítás', FALSE),
    (3, 'HU85117730161111101800000002', INTERVAL '13 days 20:15',  'OUTCOME', 245.50, 'Booking.com',        'Szállásfoglalás',            'Utazás',       TRUE),
    (4, 'HU85117730161111101800000002', INTERVAL '8 days 14:40',   'OUTCOME', 39.99,  'Amazon.de',          'Online vásárlás',            'Vásárlás',     TRUE)
) AS v(m, iban, at_time, type, amount, partner, message, category, with_card) ON v.m = mo.m
WHERE mo.month_start + v.at_time <= now();

-- Anna és Bence egymásnak utal: mindkét oldalon megjelenik a tétel
INSERT INTO demo_events (account_number, event_time, type, amount, partner_name, message, category, from_account, to_account)
SELECT v.iban, mo.month_start + v.at_time, v.type, v.amount, v.partner, v.message, v.category, v.from_account, v.to_account
FROM demo_months mo
CROSS JOIN (VALUES
    ('HU15117730161111101800000001', INTERVAL '11 days 20:30', 'TRANSFER_OUT', 7500, 'Nagy Bence',   'Közös vacsora', 'Étterem, kávézó', 'HU15117730161111101800000001', 'HU28104000950000521700000003'),
    ('HU28104000950000521700000003', INTERVAL '11 days 20:30', 'TRANSFER_IN',  7500, 'Kovács Anna',  'Közös vacsora', NULL,              'HU15117730161111101800000001', 'HU28104000950000521700000003'),
    ('HU28104000950000521700000003', INTERVAL '24 days 12:00', 'TRANSFER_OUT', 3000, 'Kovács Anna',  'Mozijegy',      'Szórakozás',      'HU28104000950000521700000003', 'HU15117730161111101800000001'),
    ('HU15117730161111101800000001', INTERVAL '24 days 12:00', 'TRANSFER_IN',  3000, 'Nagy Bence',   'Mozijegy',      NULL,              'HU28104000950000521700000003', 'HU15117730161111101800000001')
) AS v(iban, at_time, type, amount, partner, message, category, from_account, to_account)
WHERE mo.month_start + v.at_time <= now();

-- Anna megtakarításai (az egyenleget a lenti blokk könyveli)
INSERT INTO savings_accounts (user_id, name, balance, currency, interest_rate, target_amount, status, created_at, updated_at)
SELECT u.id, v.name, 0, 'HUF', 3.50, v.target_amount, 'ACTIVE', mo.month_start + v.at_time, mo.month_start + v.at_time
FROM users u
JOIN demo_months mo ON mo.m = 0
CROSS JOIN (VALUES
    ('Nyaralás', 600000::numeric, INTERVAL '6 days 09:00'),
    ('Vésztartalék', NULL::numeric, INTERVAL '8 days 09:00')
) AS v(name, target_amount, at_time)
WHERE u.email = 'anna@finex.hu';

DO $$
DECLARE
    e                 RECORD;
    v_account_id      BIGINT;
    v_currency        VARCHAR(3);
    v_balance         NUMERIC(18,2);
    v_tx_id           BIGINT;
    v_savings_id      BIGINT;
    v_savings_balance NUMERIC(18,2);
    v_amount          NUMERIC(18,2);
BEGIN
    -- 1) Tételek könyvelése időrendben, futó egyenleggel és egyenlegtörténettel
    FOR e IN SELECT * FROM demo_events ORDER BY event_time, account_number LOOP
        UPDATE accounts
        SET balance = balance + CASE WHEN e.type IN ('INCOME', 'TRANSFER_IN') THEN e.amount ELSE -e.amount END
        WHERE account_number = e.account_number
        RETURNING id, currency, balance INTO v_account_id, v_currency, v_balance;

        INSERT INTO transactions (account_id, card_id, type, amount, currency, message, partner_name, from_account, to_account, created_at)
        VALUES (
            v_account_id,
            CASE WHEN e.with_card THEN (SELECT c.id FROM cards c WHERE c.account_id = v_account_id ORDER BY c.id LIMIT 1) END,
            e.type,
            e.amount,
            v_currency,
            e.message,
            e.partner_name,
            COALESCE(e.from_account, CASE WHEN e.type IN ('OUTCOME', 'TRANSFER_OUT') THEN e.account_number END),
            COALESCE(e.to_account, CASE WHEN e.type IN ('INCOME', 'TRANSFER_IN') THEN e.account_number END),
            e.event_time)
        RETURNING id INTO v_tx_id;

        IF e.category IS NOT NULL THEN
            INSERT INTO transaction_categories (transaction_id, category_id)
            SELECT v_tx_id, c.id FROM categories c WHERE c.name = e.category;
        END IF;

        INSERT INTO balance_history (account_id, balance, created_at)
        VALUES (v_account_id, v_balance, e.event_time);
    END LOOP;

    -- 2) Megtakarítások: a befizetések és a hónap eleji kamatjóváírás (évi 3,5% / 12) időrendben
    FOR e IN
        SELECT x.*
        FROM (
            SELECT de.event_time, 'DEPOSIT' AS kind, de.amount, substring(de.message FROM 'Megtakarítás: (.*)') AS savings_name
            FROM demo_events de
            WHERE de.message LIKE 'Megtakarítás: %'
            UNION ALL
            SELECT mo.month_start + INTERVAL '2 hours', 'INTEREST', 0, s.name
            FROM demo_months mo
            CROSS JOIN (VALUES ('Nyaralás'), ('Vésztartalék')) AS s(name)
            WHERE mo.m > 0 AND mo.month_start + INTERVAL '2 hours' <= now()
        ) x
        ORDER BY x.event_time
    LOOP
        SELECT sa.id, sa.balance INTO v_savings_id, v_savings_balance
        FROM savings_accounts sa
        JOIN users u ON u.id = sa.user_id
        WHERE u.email = 'anna@finex.hu' AND sa.name = e.savings_name;

        v_amount := CASE WHEN e.kind = 'DEPOSIT' THEN e.amount ELSE round(v_savings_balance * 3.50 / 100 / 12, 2) END;

        IF v_amount > 0 THEN
            UPDATE savings_accounts
            SET balance = balance + v_amount, updated_at = e.event_time
            WHERE id = v_savings_id
            RETURNING balance INTO v_savings_balance;

            INSERT INTO savings_transactions (savings_account_id, type, amount, balance_after, created_at)
            VALUES (v_savings_id, e.kind, v_amount, v_savings_balance, e.event_time);
        END IF;
    END LOOP;
END $$;

INSERT INTO beneficiaries (user_id, name, account_number, note, created_at)
SELECT u.id, v.name, v.iban, v.note, now() - INTERVAL '5 months'
FROM (VALUES
    ('anna@finex.hu', 'Nagy Bence', 'HU28104000950000521700000003', 'Barát'),
    ('anna@finex.hu', 'Tóth Gábor', 'HU66109180010000048900240017', 'Főbérlő'),
    ('anna@finex.hu', 'Kovács Mária', 'HU73117520491234567800000009', 'Anyu'),
    ('bence@finex.hu', 'Kovács Anna', 'HU15117730161111101800000001', NULL)
) AS v(email, name, iban, note)
JOIN users u ON u.email = v.email;

-- Anna albérleti díja rendszeres átutalásként: minden hónap 6-án
INSERT INTO standing_orders (account_id, to_account_number, partner_name, amount, message, frequency, next_execution_date, active, last_execution_at, created_at)
SELECT a.id, 'HU66109180010000048900240017', 'Tóth Gábor', 220000, 'Albérlet', 'MONTHLY',
       CASE
           WHEN extract(day FROM now() AT TIME ZONE 'Europe/Budapest') < 6
               THEN (date_trunc('month', now() AT TIME ZONE 'Europe/Budapest') + INTERVAL '5 days')::date
           ELSE (date_trunc('month', now() AT TIME ZONE 'Europe/Budapest') + INTERVAL '1 month 5 days')::date
       END,
       TRUE,
       (SELECT max(t.created_at) FROM transactions t WHERE t.account_id = a.id AND t.message = 'Albérlet'),
       (SELECT min(mo.month_start) FROM demo_months mo)
FROM accounts a
WHERE a.account_number = 'HU15117730161111101800000001';

INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
SELECT u.id, v.type, v.title, v.message, v.is_read, now() - v.ago
FROM (VALUES
    ('anna@finex.hu', 'SYSTEM', 'Üdvözlünk a FineX-ben!', 'A folyószámládat és a bankkártyádat már használhatod.', TRUE, INTERVAL '6 months'),
    ('anna@finex.hu', 'SECURITY', 'Sikertelen bejelentkezési kísérlet', 'Hibás jelszóval próbáltak belépni a fiókodba. Ha nem te voltál, változtass jelszót.', TRUE, INTERVAL '5 days'),
    ('anna@finex.hu', 'SUPPORT', 'Válasz érkezett', 'Az ügyfélszolgálat válaszolt a „Nem látom a kártyás vásárlásomat” ticketedre.', FALSE, INTERVAL '2 days'),
    ('anna@finex.hu', 'SAVINGS', 'Kamatjóváírás', 'Jóváírtuk a havi kamatot a megtakarításaidon.', FALSE, INTERVAL '1 day'),
    ('anna@finex.hu', 'TRANSACTION', 'Beérkező utalás', '3 000 Ft érkezett Nagy Bence számlájáról.', FALSE, INTERVAL '3 hours'),
    ('bence@finex.hu', 'SYSTEM', 'Üdvözlünk a FineX-ben!', 'A folyószámládat és a bankkártyádat már használhatod.', TRUE, INTERVAL '6 months'),
    ('admin@finex.hu', 'SYSTEM', 'Üdvözlünk a FineX-ben!', 'Adminisztrátori jogosultsággal léptél be.', TRUE, INTERVAL '6 months')
) AS v(email, type, title, message, is_read, ago)
JOIN users u ON u.email = v.email;

INSERT INTO support_tickets (user_id, title, message, status, created_at, updated_at)
SELECT u.id, v.title, v.message, v.status, now() - v.created_ago, now() - v.updated_ago
FROM (VALUES
    ('anna@finex.hu', 'Nem látom a kártyás vásárlásomat', 'Tegnap fizettem a kártyámmal a Sparban, de a tétel nem jelenik meg a tranzakcióim között. Mit tegyek?', 'IN_PROGRESS', INTERVAL '3 days', INTERVAL '2 days'),
    ('bence@finex.hu', 'Devizaszámla nyitása', 'Szeretnék euró alapú számlát nyitni. Ezt meg tudom tenni az alkalmazásban?', 'OPEN', INTERVAL '1 day', INTERVAL '1 day')
) AS v(email, title, message, status, created_ago, updated_ago)
JOIN users u ON u.email = v.email;

INSERT INTO support_ticket_messages (ticket_id, author_id, message, created_at)
SELECT t.id, admin.id,
       'Kedves Anna! A kártyás tételek általában 1-2 munkanapon belül könyvelődnek. Ha holnapig sem jelenik meg, jelezd itt, és utánanézünk.',
       now() - INTERVAL '2 days'
FROM support_tickets t
JOIN users u ON u.id = t.user_id AND u.email = 'anna@finex.hu'
CROSS JOIN users admin
WHERE admin.email = 'admin@finex.hu';

INSERT INTO login_logs (user_id, email, status, ip_address, user_agent, failure_reason, created_at)
SELECT u.id, v.email, v.status, v.ip, v.agent, v.reason, now() - v.ago
FROM (VALUES
    ('anna@finex.hu', 'SUCCESS', '84.236.12.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36', NULL, INTERVAL '20 days'),
    ('anna@finex.hu', 'SUCCESS', '84.236.12.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.6 Mobile/15E148 Safari/604.1', NULL, INTERVAL '9 days'),
    ('anna@finex.hu', 'FAILED', '185.43.207.8', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0 Safari/537.36', 'Hibás jelszó', INTERVAL '5 days 2 hours'),
    ('anna@finex.hu', 'FAILED', '185.43.207.8', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0 Safari/537.36', 'Hibás jelszó', INTERVAL '5 days 1 hour'),
    ('anna@finex.hu', 'SUCCESS', '84.236.12.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36', NULL, INTERVAL '2 days'),
    ('bence@finex.hu', 'SUCCESS', '91.120.44.17', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.6 Safari/605.1.15', NULL, INTERVAL '4 days'),
    ('bence@finex.hu', 'SUCCESS', '91.120.44.17', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.6 Safari/605.1.15', NULL, INTERVAL '1 day'),
    ('admin@finex.hu', 'SUCCESS', '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36', NULL, INTERVAL '1 day')
) AS v(email, status, ip, agent, reason, ago)
JOIN users u ON u.email = v.email;

-- Elgépelt e-mail címmel érkezett próbálkozás: felhasználóhoz nem köthető, de naplózva van
INSERT INTO login_logs (user_id, email, status, ip_address, user_agent, failure_reason, created_at)
VALUES (NULL, 'anna@finex.com', 'FAILED', '185.43.207.8',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0 Safari/537.36',
        'Ismeretlen e-mail cím', now() - INTERVAL '5 days 3 hours');
