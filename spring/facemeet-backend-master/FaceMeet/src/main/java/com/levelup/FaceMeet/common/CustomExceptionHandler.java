package com.levelup.FaceMeet.common;

import com.levelup.FaceMeet.dto.ErrorDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorDTO> handleCustomException(CustomException e, HttpServletRequest request) {

        String requestURI = request.getRequestURI();

        log.error("=== CustomException 발생 ===");
        log.error("에러 코드: {}", e.getErrorCode());
        log.error("에러 메시지: {}", e.getMessage());
        log.error("요청 URL: {}", requestURI);

        ErrorCode errorCode = e.getErrorCode();
        String message = e.getCustomMessage() != null ? e.getCustomMessage() : errorCode.getMessage();

        return ResponseEntity.status(errorCode.getStatus().value())
                .body(ErrorDTO.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<ErrorDTO> handleHttpClientError(HttpClientErrorException e, HttpServletRequest request) {
        HttpStatusCode status = e.getStatusCode();
        ErrorCode errorCode = switch (status.value()) {
            case 400 -> ErrorCode.BAD_REQUEST;
            case 401 -> ErrorCode.UNAUTHORIZED_ACCESS;
            case 403 -> ErrorCode.ACCESS_DENIED;
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };

        log.error("클라이언트 에러 발생 - 상태코드: {}, URL: {}", status.value(), request.getRequestURI());
        return ErrorDTO.toResponseEntity(errorCode);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    protected ResponseEntity<ErrorDTO> handleHttpServerError(HttpServerErrorException e, HttpServletRequest request) {
        HttpStatusCode status = e.getStatusCode();
        ErrorCode errorCode = switch (status.value()) {
            case 502 -> ErrorCode.BAD_GATEWAY;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };

        log.error("서버 에러 발생 - 상태코드: {}, URL: {}", status.value(), request.getRequestURI());
        return ErrorDTO.toResponseEntity(errorCode);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorDTO> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        log.error("=== 일반 Exception 발생 ===");
        log.error("예외 타입: {}", e.getClass().getSimpleName());
        log.error("예외 메시지: {}", e.getMessage(), e);
        log.error("요청 URL: {}", requestURI);

        ErrorCode errorCode;

        return ErrorDTO.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
