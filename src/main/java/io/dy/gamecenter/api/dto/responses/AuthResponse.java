package io.dy.gamecenter.api.dto.responses;

import lombok.Data;

@Data
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private long accessTokenExpiredTime;

    private long refreshTokenExpiredTime;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, long accessTokenExpiredTime, long refreshTokenExpiredTime) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiredTime = accessTokenExpiredTime;
        this.refreshTokenExpiredTime = refreshTokenExpiredTime;
    }
}
