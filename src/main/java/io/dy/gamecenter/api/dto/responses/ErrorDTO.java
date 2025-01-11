package io.dy.gamecenter.api.dto.responses;

import io.dy.gamecenter.api.constants.ApiErrorEnum;
import lombok.Data;

@Data
public class ErrorDTO {

    private String message;

    private int code;

    public ErrorDTO() {}

    public ErrorDTO(ApiErrorEnum error) {
        this.code = error.getCode();
        this.message = error.getMessage();
    }
}
