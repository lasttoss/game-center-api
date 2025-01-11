package io.dy.gamecenter.api.controllers;

import io.dy.gamecenter.api.dto.requests.UserDataRequest;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.services.UserDataService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping(value = "/api/v1/users/data")
public class UserDataController {

    @Autowired
    private UserDataService userDataService;

    @PostMapping(value = "")
    public ResponseEntity<Response> save(Principal principal, UserDataRequest request) {
        String userId = principal.getName();
        Response response = userDataService.save(userId, request);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
