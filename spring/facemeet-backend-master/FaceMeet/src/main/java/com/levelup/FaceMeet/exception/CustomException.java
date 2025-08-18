package com.levelup.FaceMeet.exception;

import com.levelup.FaceMeet.dto.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;
  private final String customMessage;

  public CustomException(ErrorCode errorCode) {
    this(errorCode, null);
  }

  public CustomException(ErrorCode errorCode, String customMessage) {
    super(customMessage != null ? customMessage : errorCode.getMessage());
    this.errorCode = errorCode;
    this.customMessage = customMessage;
  }
}
