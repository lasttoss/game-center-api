package io.dy.gamecenter.api.controllers;

import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.services.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping(value = "/{gameId}/{type}")
    public ResponseEntity<Response> listByGameId(@PathVariable String gameId, @PathVariable int type) {
        Response response = leaderboardService.list(gameId, type);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
