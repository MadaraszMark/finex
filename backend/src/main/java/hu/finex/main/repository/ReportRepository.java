package hu.finex.main.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hu.finex.main.dto.MonthlySummaryResponse;
import hu.finex.main.dto.ReconciliationItemResponse;
import hu.finex.main.dto.StatementItemResponse;
import hu.finex.main.model.enums.TransactionType;
import lombok.RequiredArgsConstructor;

// Az adatbázisban megírt SQL-függvények és nézetek lekérdezése natív SQL-lel

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // A számla egyenlege egy adott időpontban (account_balance_at függvény)
    public BigDecimal findBalanceAt(Long accountId, Instant at) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountId", accountId)
                .addValue("at", toOffsetDateTime(at));

        return jdbcTemplate.queryForObject("select account_balance_at(:accountId, :at)", params, BigDecimal.class);
    }

    // Az időszak tételei futó egyenleggel (account_statement függvény)
    public List<StatementItemResponse> findStatement(Long accountId, Instant from, Instant to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountId", accountId)
                .addValue("from", toOffsetDateTime(from))
                .addValue("to", toOffsetDateTime(to));

        return jdbcTemplate.query("select * from account_statement(:accountId, :from, :to)", params, (rs, rowNum) -> StatementItemResponse.builder()
                .transactionId(rs.getLong("transaction_id"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class).toInstant())
                .type(TransactionType.valueOf(rs.getString("type")))
                .partnerName(rs.getString("partner_name"))
                .message(rs.getString("message"))
                .amount(rs.getBigDecimal("amount"))
                .signedAmount(rs.getBigDecimal("signed_amount"))
                .runningBalance(rs.getBigDecimal("running_balance"))
                .build());
    }

    // Havi bevétel és kiadás a megadott számlákon összesítve (v_account_monthly_summary nézet)
    public List<MonthlySummaryResponse> findMonthlySummary(Collection<Long> accountIds, LocalDate fromMonth) {
        if (accountIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountIds", accountIds)
                .addValue("fromMonth", fromMonth);

        String sql = "select month, sum(income) as income, sum(outcome) as outcome, sum(transaction_count) as transaction_count"
                + " from v_account_monthly_summary"
                + " where account_id in (:accountIds) and month >= :fromMonth"
                + " group by month order by month";

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> MonthlySummaryResponse.builder()
                .month(rs.getObject("month", LocalDate.class).format(MONTH_FORMAT))
                .income(rs.getBigDecimal("income"))
                .outcome(rs.getBigDecimal("outcome"))
                .transactionCount(rs.getLong("transaction_count"))
                .build());
    }

    // Főkönyvi egyeztetés: ahol a tárolt egyenleg eltér a tételekből számolttól (helyesen üres lista)
    public List<ReconciliationItemResponse> findReconciliationMismatches() {
        String sql = "select 'ACCOUNT' as kind, account_id as id, account_number as label, currency, stored_balance, ledger_balance, difference"
                + " from v_account_reconciliation where difference <> 0"
                + " union all"
                + " select 'SAVINGS', savings_account_id, name, currency, stored_balance, ledger_balance, difference"
                + " from v_savings_reconciliation where difference <> 0 or last_balance_after is distinct from (case when transaction_count = 0 then null else stored_balance end)"
                + " order by kind, id";

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> ReconciliationItemResponse.builder()
                .kind(rs.getString("kind"))
                .id(rs.getLong("id"))
                .label(rs.getString("label"))
                .currency(rs.getString("currency"))
                .storedBalance(rs.getBigDecimal("stored_balance"))
                .ledgerBalance(rs.getBigDecimal("ledger_balance"))
                .difference(rs.getBigDecimal("difference"))
                .build());
    }

    // Az egyeztetett számlák és megtakarítások száma
    public long countReconciledAccounts() {
        return jdbcTemplate.queryForObject("select count(*) from v_account_reconciliation", new MapSqlParameterSource(), Long.class);
    }

    public long countReconciledSavings() {
        return jdbcTemplate.queryForObject("select count(*) from v_savings_reconciliation", new MapSqlParameterSource(), Long.class);
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
