package info.app.juleaften.exception;

import info.app.juleaften.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        LOGGER.warn(e.getMessage());
        LOGGER.debug(Arrays.toString(e.getStackTrace()));

        if (e instanceof ExceptionCodeAware exceptionCodeAware) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(exceptionCodeAware.getExceptionCode()));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage()));
        }
    }
}
