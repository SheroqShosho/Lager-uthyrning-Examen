package se.gritacademy.lageruthyrningexamen.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.gritacademy.lageruthyrningexamen.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StringIndexOutOfBoundsException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageUnitUnavailable(
            StorageUnitUnavailableException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                ));

    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        // Använder IllegalArgumentException för "User not found" just nu => gör den till 400
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
