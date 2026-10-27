package hu.finex.main.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.CreateStandingOrderRequest;
import hu.finex.main.dto.StandingOrderResponse;
import hu.finex.main.dto.UpdateStandingOrderRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.StandingOrderMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.StandingOrder;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.StandingOrderFrequency;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.StandingOrderRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import hu.finex.main.util.IbanUtils;
import hu.finex.main.util.MoneyUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StandingOrderService {

    private final StandingOrderRepository standingOrderRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final StandingOrderMapper standingOrderMapper;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;

    @Transactional(readOnly = true)
    public List<StandingOrderResponse> listMine() {
        return standingOrderRepository.findByAccount_User_IdOrderByCreatedAtDesc(currentUser.requireId()).stream().map(standingOrderMapper::toResponse).toList();
    }

    @Transactional
    public StandingOrderResponse create(CreateStandingOrderRequest request) {
        Account account = accountRepository.findByIdAndUser_Id(request.getAccountId(), currentUser.requireId()).orElseThrow(() -> new NotFoundException("Forrás számla nem található."));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Nem aktív számláról nem indítható rendszeres átutalás.");
        }
        String toAccountNumber = IbanUtils.normalize(request.getToAccountNumber());
        if (!IbanUtils.isValid(toAccountNumber)) {
            throw new BusinessException("Érvénytelen számlaszám (IBAN).");
        }
        if (toAccountNumber.equals(account.getAccountNumber())) {
            throw new BusinessException("A forrás- és a célszámla nem lehet ugyanaz.");
        }
        if (standingOrderRepository.countByAccount_User_IdAndActiveTrue(account.getUser().getId()) >= finexProperties.getMaxStandingOrdersPerUser()) {
            throw new BusinessException("Legfeljebb " + finexProperties.getMaxStandingOrdersPerUser() + " aktív rendszeres átutalásod lehet.");
        }

        StandingOrder order = standingOrderMapper.toEntity(request, account, toAccountNumber);
        order = standingOrderRepository.save(order);

        return standingOrderMapper.toResponse(order);
    }

    // Módosítás: összeg, közlemény, gyakoriság, esedékesség (a címzett nem változtatható)
    @Transactional
    public StandingOrderResponse update(Long id, UpdateStandingOrderRequest request) {
        StandingOrder order = findOwned(id);

        if (!order.isActive()) {
            throw new BusinessException("Megszüntetett megbízás nem módosítható.");
        }

        standingOrderMapper.updateEntity(order, request);
        return standingOrderMapper.toResponse(order);
    }

    // Megszüntetés: a megbízás megmarad az előzményekben, de többé nem teljesül
    @Transactional
    public void cancel(Long id) {
        StandingOrder order = findOwned(id);
        order.setActive(false);
    }

    @Transactional(readOnly = true)
    public List<Long> findDueOrderIds() {
        return standingOrderRepository.findDueOrderIds(DateUtils.today());
    }

    // Saját tranzakcióban fut, és zárolva újra ellenőrzi az esedékességet, így nem teljesülhet kétszer
    @Transactional
    public void execute(Long orderId) {
        StandingOrder order = standingOrderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new NotFoundException("Megbízás nem található."));

        if (!order.isActive() || order.getNextExecutionDate().isAfter(DateUtils.today())) {
            return;
        }

        // A tulajdonost a számla betöltése nélkül kérdezzük le: a számlát az utalás zárolja és olvassa be
        Long ownerId = standingOrderRepository.findOwnerIdById(orderId).orElseThrow(() -> new NotFoundException("Megbízás nem található."));
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        // Letiltott ügyfél megbízása nem teljesül (a sikertelen teljesítésként rögzített hiba továbblépteti)
        if (owner.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Az ügyfél fiókja le van tiltva.");
        }

        transactionService.executeTransfer(owner, order.getAccount().getId(), order.getToAccountNumber(), order.getPartnerName(),
                order.getAmount(), order.getMessage(), null);

        order.setLastExecutionAt(Instant.now());
        order.setNextExecutionDate(nextExecutionDate(order));

        notificationService.notify(owner, NotificationType.TRANSACTION, "Rendszeres átutalás teljesült",
                MoneyUtils.format(order.getAmount(), order.getAccount().getCurrency()) + " utalva " + order.getPartnerName() + " részére.");
    }

    // Sikertelen teljesítés: a megbízás továbblép, a tulajdonos értesítést kap (új tranzakcióban)
    @Transactional
    public void handleFailedExecution(Long orderId, String reason) {
        StandingOrder order = standingOrderRepository.findByIdForUpdate(orderId).orElse(null);
        if (order == null || order.getNextExecutionDate().isAfter(DateUtils.today())) {
            return;
        }

        order.setNextExecutionDate(nextExecutionDate(order));

        notificationService.notify(order.getAccount().getUser(), NotificationType.TRANSACTION, "Sikertelen rendszeres átutalás",
                "A(z) " + order.getPartnerName() + " részére szóló rendszeres átutalás nem teljesült: " + reason);
    }

    private StandingOrder findOwned(Long id) {
        return standingOrderRepository.findByIdAndAccount_User_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Megbízás nem található."));
    }

    // Havi megbízás a teljesítés napjához igazodik: a 31-i februárban a hónap végén, márciusban 31-én fut
    static LocalDate nextExecutionDate(StandingOrder order, LocalDate today) {
        LocalDate next = order.getNextExecutionDate();

        while (!next.isAfter(today)) {
            if (order.getFrequency() == StandingOrderFrequency.WEEKLY) {
                next = next.plusWeeks(1);
            } else {
                LocalDate nextMonth = next.withDayOfMonth(1).plusMonths(1);
                next = nextMonth.withDayOfMonth(Math.min(order.getExecutionDay(), nextMonth.lengthOfMonth()));
            }
        }
        return next;
    }

    private LocalDate nextExecutionDate(StandingOrder order) {
        return nextExecutionDate(order, DateUtils.today());
    }
}
