package cn.hrbzhongjiebang.web;

import cn.hrbzhongjiebang.common.BusinessException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<Map<String, Object>> handleBusiness(BusinessException error) {
        HttpStatus status = switch (error.code()) {
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN", "MEMBERSHIP_REQUIRED", "DAILY_LIMIT_REACHED" -> HttpStatus.FORBIDDEN;
            case "HOUSE_NOT_FOUND", "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "PHONE_REGISTERED", "REDEEM_CODE_USED" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Map.of(
                "code", error.code(), "message", error.getMessage(), "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException error) {
        String message = error.getBindingResult().getFieldErrors().stream()
                .findFirst().map(field -> field.getDefaultMessage()).orElse("请求参数不正确");
        return ResponseEntity.badRequest().body(Map.of("code", "VALIDATION_FAILED", "message", message));
    }
}
