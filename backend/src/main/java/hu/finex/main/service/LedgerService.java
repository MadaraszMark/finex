package hu.finex.main.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.exception.BusinessException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.Card;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

// Könyvelés egy helyen: minden egyenlegváltozás ezen megy át, a hívó előtte zárolja a számlát

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final TransactionRepository transactionRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final BalanceHistoryMapper balanceHistoryMapper;

    // Jóváírás (INCOME vagy TRANSFER_IN): az egyenleg nő
    @Transactional(propagation = Propagation.MANDATORY)
    public Transaction credit(Account account, TransactionType type, BigDecimal amount, String partnerName, String message, String fromAccount) {
        return creditLinked(account, type, amount, partnerName, message, fromAccount, null);
    }

    // Jóváírás egy terhelés párjaként (belső utalás, devizaváltás): a tétel a terhelésre mutat
    @Transactional(propagation = Propagation.MANDATORY)
    public Transaction creditLinked(Account account, TransactionType type, BigDecimal amount, String partnerName, String message, String fromAccount, Transaction related) {
        validateAmount(amount);

        account.setBalance(account.getBalance().add(amount));

        return book(account, null, type, amount, partnerName, message, fromAccount, account.getAccountNumber(), related, null);
    }

    // Terhelés (OUTCOME vagy TRANSFER_OUT): fedezet nélkül nem engedi
    @Transactional(propagation = Propagation.MANDATORY)
    public Transaction debit(Account account, TransactionType type, BigDecimal amount, String partnerName, String message, String toAccount, Card card) {
        validateAmount(amount);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Nincs elegendő fedezet a számlán.");
        }

        account.setBalance(account.getBalance().subtract(amount));

        return book(account, card, type, amount, partnerName, message, account.getAccountNumber(), toAccount, null, null);
    }

    // Sztornó: ellentétes irányú javító tétel, az eredeti érintetlen marad (append-only főkönyv)
    @Transactional(propagation = Propagation.MANDATORY)
    public Transaction reverse(Account account, Transaction original, String reason) {
        BigDecimal amount = original.getAmount();
        TransactionType type = switch (original.getType()) {
            case INCOME -> TransactionType.OUTCOME;
            case OUTCOME -> TransactionType.INCOME;
            case TRANSFER_IN -> TransactionType.TRANSFER_OUT;
            case TRANSFER_OUT -> TransactionType.TRANSFER_IN;
        };

        if (type == TransactionType.OUTCOME || type == TransactionType.TRANSFER_OUT) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("Nincs elegendő fedezet a sztornóhoz a(z) " + account.getAccountNumber() + " számlán.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }

        return book(account, null, type, amount, original.getPartnerName(), "Sztornó: " + reason, original.getToAccount(), original.getFromAccount(), null, original);
    }

    private Transaction book(Account account, Card card, TransactionType type, BigDecimal amount, String partnerName, String message,
                             String fromAccount, String toAccount, Transaction related, Transaction reversalOf) {
        Transaction transaction = Transaction.builder()
                .account(account)
                .card(card)
                .type(type)
                .amount(amount)
                .currency(account.getCurrency())
                .message(message)
                .partnerName(partnerName)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .relatedTransaction(related)
                .reversalOf(reversalOf)
                .build();
        transaction = transactionRepository.save(transaction);

        balanceHistoryRepository.save(balanceHistoryMapper.toEntity(account, account.getBalance()));

        return transaction;
    }

    // Csak pozitív, legfeljebb két tizedesjegyű összeg (az adatbázis is NUMERIC(18,2)-ben tárolja)
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("Az összegnek pozitívnak kell lennie.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("Az összeg legfeljebb két tizedesjegyet tartalmazhat.");
        }
    }
}
