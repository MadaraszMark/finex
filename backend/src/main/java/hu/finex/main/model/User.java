package hu.finex.main.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = { @Index(name = "uk_users_email", columnList = "email", unique = true)})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class User {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
	private Long id;
	
	@NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
	private String firstName;
	
	@NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
	private String lastName;
	
	@Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
	private String email;
	
	@Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;
	
	@JsonIgnore //JSON-ba sose küldjük ki
    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;
	
	@NotBlank
    @Size(max = 32)
    @Column(nullable = false, length = 32)
    private String role;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
