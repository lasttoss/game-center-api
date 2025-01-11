package io.dy.gamecenter.api.controllers;

import io.dy.gamecenter.api.dto.requests.MatchRequest;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.services.MatchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping(value = "/api/v1/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping(value = "")
    public ResponseEntity<Response> create(Principal principal, @RequestHeader("x-game-id") String gameId, @RequestHeader("x-api-key") String gameKey) {
        String userId = principal.getName();
        Response response = matchService.create(userId, gameId, gameKey);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/{matchId}")
    public ResponseEntity<Response> update(Principal principal, @RequestHeader("x-game-id") String gameId,
                                           @RequestHeader("x-api-key") String gameKey, @PathVariable String matchId, @RequestBody MatchRequest request) {
        String userId = principal.getName();
        Response response = matchService.update(matchId, request, userId, gameId, gameKey);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
