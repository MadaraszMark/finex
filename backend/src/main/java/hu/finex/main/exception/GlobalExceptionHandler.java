package hu.finex.main.exception;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {

        List<Map<String, String>> violations = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> Map.of(
                        "field", e.getField(),
                        "message", e.getDefaultMessage()))
                .toList();

        ApiError error = ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Érvénytelen kérés, hibás mezők")
                .violations(violations)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // Hibás formátumú kérés: nem értelmezhető JSON, rossz enum érték, szöveg szám helyett
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Hibás formátumú kérés.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, "Érvénytelen paraméter: " + ex.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex) {
        return error(HttpStatus.BAD_REQUEST, "Hiányzó paraméter: " + ex.getParameterName());
    }

    // A könyvtárak belső (angol, technikai) hibaüzenete nem megy ki a kliensnek, csak a naplóba
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Érvénytelen argumentum: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, "Érvénytelen kérés.");
    }

    // Lapozásnál nem létező mező szerinti rendezés (?sort=...)
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handlePropertyReference(PropertyReferenceException ex) {
        return error(HttpStatus.BAD_REQUEST, "Érvénytelen rendezési mező: " + ex.getPropertyName());
    }

    // A Spring Data a hibás lekérdezési paramétert (pl. rendezést) gyakran ebbe csomagolja
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiError> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        log.warn("Érvénytelen lekérdezés: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.BAD_REQUEST, "Érvénytelen kérés.");
    }

    // Validációs hiba útvonal- vagy lekérdezési paraméteren (nem a kérés törzsében)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        return error(HttpStatus.BAD_REQUEST, "Érvénytelen kérés, hibás paraméter.");
    }

    // Értelmezhetetlen vagy a naptár határain kívül eső dátum (pl. a +999999999. év)
    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ApiError> handleDateTime(DateTimeException ex) {
        return error(HttpStatus.BAD_REQUEST, "Érvénytelen dátum.");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException ex) {
        return error(HttpStatus.BAD_REQUEST, "Hiányzó fejléc: " + ex.getHeaderName());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "A kérés formátuma nem támogatott (JSON szükséges).");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return error(HttpStatus.NOT_ACCEPTABLE, "A kért válaszformátum nem támogatott.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Nincs jogosultságod ehhez a művelethez.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Nincs ilyen végpont.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Ez a HTTP metódus itt nem támogatott.");
    }

    // Adatbázis-szabály sértése: csak a szabály neve naplózódik (az üzenet ütköző adatot is tartalmazna)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String constraint = ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException violation ? violation.getConstraintName() : null;
        log.warn("Adatbázis-szabály sértése: {}", constraint != null ? constraint : ex.getMostSpecificCause().getClass().getSimpleName());
        return error(HttpStatus.CONFLICT, "Az adatbázis elutasította a műveletet (ütközés vagy szabálysértés).");
    }

    // Sorzárolási hiba (pl. holtpontnál az adatbázis megszakítja az egyik tranzakciót)
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleLockingFailure(PessimisticLockingFailureException ex) {
        return error(HttpStatus.CONFLICT, "A számlán éppen egy másik művelet fut, próbáld újra.");
    }

    // Belső hibaüzenet nem megy ki a kliensnek, csak a naplóba
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Váratlan hiba", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Váratlan hiba történt.");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError error = ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
