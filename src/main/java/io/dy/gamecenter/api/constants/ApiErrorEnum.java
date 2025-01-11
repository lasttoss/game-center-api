package io.dy.gamecenter.api.constants;

public enum ApiErrorEnum {

    EXIST_USER(1, "EXIST_USER"),
    USER_NOT_FOUND(2, "USER_NOT_FOUND"),
    ITEM_NOT_FOUND(3, "ITEM_NOT_FOUND"),
    CAN_NOT_UPDATE_DATA(5, "CAN_NOT_UPDATE_DATA"),
    INVALID_REQUEST(6, "INVALID_REQUEST"),
    INVALID_RESOURCE(7, "INVALID_RESOURCE"),
    FAILED_TO_VERIFY_TOKEN(13, "FAILED_TO_VERIFY_TOKEN"),
    MATCH_NOT_FOUND(16, "MATCH_NOT_FOUND"),
    CAN_NOT_UPDATE_THIS_MATCH(17, "CAN_NOT_UPDATE_THIS_MATCH");

    private final int code;

    private final String message;

    ApiErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
