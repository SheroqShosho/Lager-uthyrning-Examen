package se.gritacademy.lageruthyrningexamen.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.gritacademy.lageruthyrningexamen.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StorageUnitUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageUnitUnavailable(
            StorageUnitUnavailableException ex,
            HttpServletRequest request
    ) {
        logger.warn("StorageUnitUnavailableException: {}", ex.getMessage());
        HttpStatus status = HttpStatus.CONFLICT; // 409
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                ));

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        logger.warn("IllegalArgumentException: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400
        return  ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                ));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        logger.error("Unexpected error: {}", ex.getClass().getSimpleName(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        return  ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        "Unexpected error",
                        request.getRequestURI()
                ));
    }
}
