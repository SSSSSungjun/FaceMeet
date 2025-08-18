package com.levelup.FaceMeet.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenResponseStatus {

    private Integer status;
    private String message;

    public static TokenResponseStatus addStatus(Integer status, String message) {
        return new TokenResponseStatus(status, message);
    }
}
