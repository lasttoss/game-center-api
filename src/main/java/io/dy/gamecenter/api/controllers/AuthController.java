package io.dy.gamecenter.api.controllers;

import io.dy.gamecenter.api.dto.requests.AuthRenewRequest;
import io.dy.gamecenter.api.dto.requests.AuthRequest;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/register")
    public ResponseEntity<Response> register(@RequestBody AuthRequest request) {
        Response response = authService.register(request);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Response> loginByAuth(@RequestBody AuthRequest request) {
        Response response = authService.login(request);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/renew")
    public ResponseEntity<Response> renewToken(@RequestBody AuthRenewRequest request) {
        Response response = authService.renewToken(request);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
