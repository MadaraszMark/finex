package hu.finex.main.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        StandingOrder order = standingOrderMapper.toEntity(request, account, toAccountNumber);
        order = standingOrderRepository.save(order);

        return standingOrderMapper.toResponse(order);
    }

    // Összeg, közlemény, gyakoriság és következő esedékesség módosítása (a címzett nem változtatható)
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

    // Az ütemező által ma teljesítendő megbízások
    @Transactional(readOnly = true)
    public List<Long> findDueOrderIds() {
        return standingOrderRepository.findDueOrderIds(DateUtils.today());
    }

    // Egy megbízás teljesítése saját tranzakcióban (az ütemező hívja egyenként). Ha nem sikerül (pl. nincs fedezet),
    // a kivétel visszagörgeti ezt a tranzakciót, és az ütemező a handleFailedExecution-nel rögzíti a hibát.
    @Transactional
    public void execute(Long orderId) {
        StandingOrder order = standingOrderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Megbízás nem található."));

        if (!order.isActive()) {
            return;
        }

        // A tulajdonost a számla betöltése nélkül kérdezzük le: a számlát az utalás zárolja és olvassa be
        Long ownerId = standingOrderRepository.findOwnerIdById(orderId).orElseThrow(() -> new NotFoundException("Megbízás nem található."));
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        transactionService.executeTransfer(owner, order.getAccount().getId(), order.getToAccountNumber(), order.getPartnerName(),
                order.getAmount(), order.getMessage(), null);

        order.setLastExecutionAt(Instant.now());
        order.setNextExecutionDate(nextExecutionDate(order));

        notificationService.notify(owner, NotificationType.TRANSACTION, "Rendszeres átutalás teljesült",
                MoneyUtils.format(order.getAmount(), order.getAccount().getCurrency()) + " utalva " + order.getPartnerName() + " részére.");
    }

    // Sikertelen teljesítés: a megbízás a következő esedékességre lép, a tulajdonos értesítést kap (új tranzakcióban)
    @Transactional
    public void handleFailedExecution(Long orderId, String reason) {
        StandingOrder order = standingOrderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }

        order.setNextExecutionDate(nextExecutionDate(order));

        notificationService.notify(order.getAccount().getUser(), NotificationType.TRANSACTION, "Sikertelen rendszeres átutalás",
                "A(z) " + order.getPartnerName() + " részére szóló rendszeres átutalás nem teljesült: " + reason);
    }

    private StandingOrder findOwned(Long id) {
        return standingOrderRepository.findByIdAndAccount_User_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Megbízás nem található."));
    }

    // A következő esedékesség: a mainál későbbi első időpont (a kimaradt időszakokat nem pótolja utólag)
    private LocalDate nextExecutionDate(StandingOrder order) {
        LocalDate next = order.getNextExecutionDate();
        LocalDate today = DateUtils.today();

        while (!next.isAfter(today)) {
            next = order.getFrequency() == StandingOrderFrequency.WEEKLY ? next.plusWeeks(1) : next.plusMonths(1);
        }
        return next;
    }
}
