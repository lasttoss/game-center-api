package io.dy.gamecenter.api.dto.responses;

import lombok.Data;

@Data
public class Response {

    private Object data;

    private int status;

    private ErrorDTO error;
}
