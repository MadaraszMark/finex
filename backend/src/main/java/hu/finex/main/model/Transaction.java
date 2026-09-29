package hu.finex.main.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import hu.finex.main.model.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Könyvelt tétel: létrehozás után nem módosul (az adatbázis triggere is tiltja az UPDATE-et és a DELETE-et)

@Entity
@Table(name = "transactions",indexes = {@Index(name = "idx_transactions_account_created", columnList = "account_id, created_at"),@Index(name = "idx_transactions_card_id", columnList = "card_id")})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Kártyás fizetésnél a használt kártya (egyébként üres)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    // Mindig pozitív, az irányt a típus adja meg
    @NotNull
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Size(max = 255)
    private String message;

    // A másik fél neve (kedvezményezett, küldő vagy kereskedő)
    @Size(max = 150)
    @Column(name = "partner_name", length = 150)
    private String partnerName;

    @Size(max = 34)
    @Column(name = "from_account", length = 34)
    private String fromAccount;

    @Size(max = 34)
    @Column(name = "to_account", length = 34)
    private String toAccount;

    @NotBlank
    @Size(max = 3)
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
