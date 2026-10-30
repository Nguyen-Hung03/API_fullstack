package com.atelier.config;

import com.atelier.api.model.ApiErrorDto;
import com.atelier.error.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorDto> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(body(ex.getCode(), ex.getMessage(), ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleBody(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return unprocessable(details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorDto> handleParams(HandlerMethodValidationException ex) {
        List<String> details = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error -> details.add(error.getDefaultMessage())));
        return unprocessable(details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorDto> handleType(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(body(
                "ERR_BAD_REQUEST",
                "Tham số không đúng kiểu",
                List.of(ex.getName() + ": giá trị không hợp lệ")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDto> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(body("ERR_BAD_REQUEST", "JSON không đọc được", List.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConflict(DataIntegrityViolationException ex) {
        return unprocessable(List.of("Dữ liệu bị trùng hoặc vi phạm ràng buộc"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleUnexpected(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("ERR_INTERNAL", "Lỗi hệ thống", List.of()));
    }

    private ResponseEntity<ApiErrorDto> unprocessable(List<String> details) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body("ERR_VALIDATION", "Dữ liệu không hợp lệ", details));
    }

    private ApiErrorDto body(String code, String message, List<String> details) {
        ApiErrorDto error = new ApiErrorDto(code, message);
        error.setDetails(details == null ? List.of() : details);
        return error;
    }
}
