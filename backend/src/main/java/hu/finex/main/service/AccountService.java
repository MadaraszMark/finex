package hu.finex.main.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.StatementItemResponse;
import hu.finex.main.dto.StatementResponse;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.ReportRepository;
import hu.finex.main.repository.StandingOrderRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.CsvUtils;
import hu.finex.main.util.DateUtils;
import hu.finex.main.util.IbanUtils;
import hu.finex.main.util.MoneyUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    // A készpénzbefizetések partnerneve (a sztornó is ez alapján ismeri fel őket)
    public static final String CASH_DEPOSIT_PARTNER = "Készpénzbefizetés";

    private static final DateTimeFormatter CSV_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter CSV_DATE_TIME = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(DateUtils.BANK_ZONE);

    private static final Map<TransactionType, String> TYPE_LABELS = Map.of(
            TransactionType.INCOME, "Jóváírás",
            TransactionType.OUTCOME, "Terhelés",
            TransactionType.TRANSFER_IN, "Bejövő utalás",
            TransactionType.TRANSFER_OUT, "Kimenő utalás");

    private static final Map<AccountStatus, String> STATUS_LABELS = Map.of(
            AccountStatus.ACTIVE, "aktív",
            AccountStatus.BLOCKED, "letiltott",
            AccountStatus.FROZEN, "befagyasztott",
            AccountStatus.CLOSED, "lezárt");

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CardService cardService;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final StandingOrderRepository standingOrderRepository;
    private final ReportRepository reportRepository;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AccountResponse> listMine() {
        return accountRepository.findByUser_IdOrderByCreatedAtAsc(currentUser.requireId()).stream().map(accountMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        return accountMapper.toResponse(findOwned(id));
    }

    // Elsődleges folyószámla: a legrégebbi aktív folyószámla (a főoldal kártyájához)
    @Transactional(readOnly = true)
    public AccountResponse getMyAccount() {
        Account account = accountRepository
                .findFirstByUser_IdAndStatusAndAccountTypeOrderByCreatedAtAsc(currentUser.requireId(), AccountStatus.ACTIVE, AccountType.CURRENT)
                .orElseThrow(() -> new NotFoundException("Számla nem található."));

        return accountMapper.toResponse(account);
    }

    // A felhasználó sorát zároljuk, így párhuzamos kérésekkel sem léphető túl a számlák száma
    @Transactional
    public AccountResponse open(CreateAccountRequest request) {
        User user = lockCurrentUser();

        if (accountRepository.countByUser_IdAndStatusNot(user.getId(), AccountStatus.CLOSED) >= finexProperties.getMaxAccountsPerUser()) {
            throw new BusinessException("Legfeljebb " + finexProperties.getMaxAccountsPerUser() + " nyitott számlád lehet.");
        }

        Account account = accountMapper.toEntity(request, user, generateUniqueAccountNumber());
        account = accountRepository.save(account);

        cardService.createCard(account);

        return accountMapper.toResponse(account);
    }

    // Regisztrációkor automatikusan létrejövő forint folyószámla, bankkártyával
    @Transactional
    public Account createDefaultAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .name("Fő számla")
                .accountNumber(generateUniqueAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .build();
        account = accountRepository.save(account);

        cardService.createCard(account);

        return account;
    }

    // Admin: készpénzbefizetés (pl. bankfiókban), ügyfél nem írhat jóvá magának pénzt
    @Transactional
    public AccountResponse depositByAdmin(Long accountId, DepositRequest request) {
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> new NotFoundException("Számla nem található."));
        ensureActive(account);

        String message = request.getMessage() != null ? request.getMessage() : "Készpénzbefizetés";
        ledgerService.credit(account, TransactionType.INCOME, request.getAmount(), CASH_DEPOSIT_PARTNER, message, null);

        auditService.record(AuditAction.ACCOUNT_DEPOSIT, AuditTargetType.ACCOUNT, account.getId(),
                account.getAccountNumber() + ": " + MoneyUtils.format(request.getAmount(), account.getCurrency()));

        notificationService.notify(account.getUser(), NotificationType.TRANSACTION, "Befizetés érkezett",
                MoneyUtils.format(request.getAmount(), account.getCurrency()) + " készpénzbefizetést írtunk jóvá a(z) " + account.getName() + " számládon.");

        return accountMapper.toResponse(account);
    }

    // Lezárás csak nulla egyenleggel, az utolsó nyitott számla nem zárható le; a kártyák megszűnnek
    @Transactional
    public void close(Long id) {
        lockCurrentUser();
        Account account = lockOwned(id);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("A számla már le van zárva.");
        }
        if (account.getBalance().signum() != 0) {
            throw new BusinessException("Csak nulla egyenlegű számla zárható le. Előbb utald át a pénzt egy másik számládra.");
        }
        if (accountRepository.countByUser_IdAndStatusNot(account.getUser().getId(), AccountStatus.CLOSED) <= 1) {
            throw new BusinessException("Az utolsó nyitott számládat nem zárhatod le.");
        }

        closeAccount(account);
    }

    // A nyitó- és futó egyenleget az adatbázis függvényei számolják, itt csak az összesítők készülnek
    @Transactional(readOnly = true)
    public StatementResponse getStatement(Long id, LocalDate from, LocalDate to) {
        Account account = findOwned(id);

        if (from.isAfter(to)) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }
        DateUtils.requireReportRange(from, to);

        Instant fromInstant = DateUtils.startOfDay(from);
        Instant toInstant = DateUtils.startOfDay(to.plusDays(1));

        BigDecimal openingBalance = reportRepository.findBalanceAt(account.getId(), fromInstant);
        List<StatementItemResponse> items = reportRepository.findStatement(account.getId(), fromInstant, toInstant);

        BigDecimal totalIncome = items.stream()
                .map(StatementItemResponse::getSignedAmount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutcome = items.stream()
                .map(StatementItemResponse::getSignedAmount)
                .filter(amount -> amount.signum() < 0)
                .map(BigDecimal::negate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal closingBalance = items.isEmpty() ? openingBalance : items.get(items.size() - 1).getRunningBalance();

        return StatementResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .from(from)
                .to(to)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .totalIncome(totalIncome)
                .totalOutcome(totalOutcome)
                .items(items)
                .build();
    }

    // Számlakivonat CSV-ben (Excelhez): nyitóegyenleg, a tételek futó egyenleggel, záróegyenleg
    @Transactional(readOnly = true)
    public byte[] exportStatementCsv(Long id, LocalDate from, LocalDate to) {
        StatementResponse statement = getStatement(id, from, to);
        String currency = statement.getCurrency();

        StringBuilder csv = new StringBuilder(CsvUtils.BOM);
        csv.append(CsvUtils.row(List.of("Dátum", "Típus", "Partner", "Közlemény", "Összeg", "Egyenleg", "Devizanem")));
        csv.append(CsvUtils.row(List.of(from.format(CSV_DATE), "Nyitóegyenleg", "", "", "", CsvUtils.number(statement.getOpeningBalance()), currency)));

        for (StatementItemResponse item : statement.getItems()) {
            csv.append(CsvUtils.row(List.of(
                    CSV_DATE_TIME.format(item.getCreatedAt()),
                    TYPE_LABELS.get(item.getType()),
                    CsvUtils.text(item.getPartnerName()),
                    CsvUtils.text(item.getMessage()),
                    CsvUtils.number(item.getSignedAmount()),
                    CsvUtils.number(item.getRunningBalance()),
                    currency)));
        }

        csv.append(CsvUtils.row(List.of(to.format(CSV_DATE), "Záróegyenleg", "", "", "", CsvUtils.number(statement.getClosingBalance()), currency)));

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Admin: összes számla, opcionálisan státusz szerint
    @Transactional(readOnly = true)
    public Page<AccountResponse> listAll(AccountStatus status, Pageable pageable) {
        Page<Account> accounts = status != null ? accountRepository.findByStatus(status, pageable) : accountRepository.findAll(pageable);
        return accounts.map(accountMapper::toResponse);
    }

    // Admin: számla befagyasztása, tiltása, feloldása vagy lezárása; a tulajdonos értesítést kap
    @Transactional
    public AccountResponse updateStatus(Long id, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Számla nem található."));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Lezárt számla státusza nem módosítható.");
        }

        AccountStatus previous = account.getStatus();
        if (request.getStatus() == AccountStatus.CLOSED) {
            if (account.getBalance().signum() != 0) {
                throw new BusinessException("Csak nulla egyenlegű számla zárható le.");
            }
            closeAccount(account);
        } else {
            accountMapper.updateStatus(account, request);
        }

        auditService.record(AuditAction.ACCOUNT_STATUS_CHANGED, AuditTargetType.ACCOUNT, account.getId(),
                account.getAccountNumber() + ": " + previous + " → " + account.getStatus());

        notificationService.notify(account.getUser(), NotificationType.SECURITY, "Számlád státusza megváltozott",
                "A(z) " + account.getName() + " számlád új státusza: " + STATUS_LABELS.get(account.getStatus()) + ".");

        return accountMapper.toResponse(account);
    }

    private void closeAccount(Account account) {
        account.setStatus(AccountStatus.CLOSED);
        cardService.cancelCardsOfAccount(account.getId());
        standingOrderRepository.findByAccount_IdAndActiveTrue(account.getId()).forEach(order -> order.setActive(false));
    }

    private User lockCurrentUser() {
        return userRepository.findByIdForUpdate(currentUser.requireId()).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));
    }

    private Account findOwned(Long id) {
        return accountRepository.findByIdAndUser_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Számla nem található."));
    }

    // Előbb ellenőrzés betöltés nélkül, aztán zárolt olvasás: így a legfrissebb egyenleggel számolunk
    private Account lockOwned(Long id) {
        if (!accountRepository.existsByIdAndUser_Id(id, currentUser.requireId())) {
            throw new NotFoundException("Számla nem található.");
        }
        return accountRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Számla nem található."));
    }

    private void ensureActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A számla nem aktív, ezért nem végezhető rajta művelet.");
        }
    }

    // Egyedi IBAN számlaszám (ütközés esetén újrapróbálkozás)
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = IbanUtils.generateHungarian();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
