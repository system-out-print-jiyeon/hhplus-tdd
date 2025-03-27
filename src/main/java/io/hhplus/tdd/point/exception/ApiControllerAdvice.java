package io.hhplus.tdd.point.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(ErrorCodes.INVALID_INPUT, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(ErrorCodes.INTERNAL_ERROR, "에러가 발생했습니다."));
    }
}
