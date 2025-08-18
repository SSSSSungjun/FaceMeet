package com.levelup.FaceMeet.security.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponse {

    private Integer status;
    private Object data;

    public StatusResponse(Integer status) {
        this.status = status;
    }

    public static StatusResponse addStatus(Integer status) {
        return new StatusResponse(status);
    }

    public static StatusResponse success(){
        return new StatusResponse(200);
    }
    public static StatusResponse success(Object data){
        return new StatusResponse(200, data);
    }
}
