package com.levelup.FaceMeet.dto;

import com.levelup.FaceMeet.exception.ErrorCode;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@Builder
public class ErrorDTO {

    private int status;
    private String code;
    private String message;

    public static ResponseEntity<ErrorDTO> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus().value())
                .body(ErrorDTO.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }
}
