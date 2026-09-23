package hu.finex.main.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {

    private OffsetDateTime timestamp;
    private int status;
    private String message;
    private List<Map<String, String>> violations;
}
