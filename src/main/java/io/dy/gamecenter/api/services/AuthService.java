package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.ApiErrorEnum;
import io.dy.gamecenter.api.constants.JwtConstants;
import io.dy.gamecenter.api.dto.requests.AuthRenewRequest;
import io.dy.gamecenter.api.dto.requests.AuthRequest;
import io.dy.gamecenter.api.dto.responses.AuthResponse;
import io.dy.gamecenter.api.dto.responses.ErrorDTO;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.UserModel;
import io.dy.gamecenter.api.repositories.UserRepository;
import io.dy.gamecenter.api.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;


    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtils jwtUtils;

    public Response register(AuthRequest request) {
        Response response = new Response();
        if (request.getUsername().length() == 0 || request.getPassword().length() == 0) {
            response.setData(null);
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.INVALID_REQUEST);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        UserModel checkUser = userRepository.findByUsername(request.getUsername());
        if (checkUser != null) {
            response.setData(null);
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.EXIST_USER);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        UserModel user = new UserModel(request.getUsername(), encodedPassword);
        user = userRepository.save(user);
        long currentTimestamp = DateTime.now().getMillis() / 1000;
        long accessTokenExpires = currentTimestamp + JwtConstants.ACCESS_TOKEN_EXPIRED_TIME;
        long refreshTokenExpires = currentTimestamp + JwtConstants.REFRESH_TOKEN_EXPIRED_TIME;
        String accessToken = jwtUtils.generateToken(user, currentTimestamp, accessTokenExpires);
        String refreshToken = jwtUtils.generateToken(user, currentTimestamp, refreshTokenExpires);
        AuthResponse auth = new AuthResponse(accessToken, refreshToken, accessTokenExpires, refreshTokenExpires);
        response.setData(auth);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    public Response login(AuthRequest request) {
        Response response = new Response();
        UserModel user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            response.setData(null);
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.USER_NOT_FOUND);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }
        long currentTimestamp = DateTime.now().getMillis() / 1000;
        long accessTokenExpires = currentTimestamp + JwtConstants.ACCESS_TOKEN_EXPIRED_TIME;
        long refreshTokenExpires = currentTimestamp + JwtConstants.REFRESH_TOKEN_EXPIRED_TIME;
        String accessToken = jwtUtils.generateToken(user, currentTimestamp, accessTokenExpires);
        String refreshToken = jwtUtils.generateToken(user, currentTimestamp, refreshTokenExpires);
        AuthResponse auth = new AuthResponse(accessToken, refreshToken, accessTokenExpires, refreshTokenExpires);
        response.setData(auth);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    public Response renewToken(AuthRenewRequest request) {
        Response response = new Response();
        if (!jwtUtils.validateToken(request.getRefreshToken())) {
            response.setData(null);
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.FAILED_TO_VERIFY_TOKEN);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        Claims token = jwtUtils.extractAllClaims(request.getRefreshToken());

        UserModel user = userRepository.findByUserId(token.getSubject());
        if (user == null) {
            response.setData(null);
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.FAILED_TO_VERIFY_TOKEN);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }
        long currentTimestamp = DateTime.now().getMillis() / 1000;
        long accessTokenExpires = currentTimestamp + JwtConstants.ACCESS_TOKEN_EXPIRED_TIME;
        long refreshTokenExpires = currentTimestamp + JwtConstants.REFRESH_TOKEN_EXPIRED_TIME;
        String accessToken = jwtUtils.generateToken(user, currentTimestamp, accessTokenExpires);
        String refreshToken = jwtUtils.generateToken(user, currentTimestamp, refreshTokenExpires);
        AuthResponse auth = new AuthResponse(accessToken, refreshToken, accessTokenExpires, refreshTokenExpires);
        response.setData(auth);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
}
