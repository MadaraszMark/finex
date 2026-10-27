package hu.finex.main.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Kijelentkezéssel visszavont token (a JWT "jti" azonosítója); a lejárata után már nem kell őrizni

@Entity
@Table(name = "revoked_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class RevokedToken {

    @Id
    @EqualsAndHashCode.Include
    @Column(length = 36)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
