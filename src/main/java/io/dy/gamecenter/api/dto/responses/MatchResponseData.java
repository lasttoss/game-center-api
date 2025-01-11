package io.dy.gamecenter.api.dto.responses;

import lombok.Data;

@Data
public class MatchResponseData {

    private String matchId;

    private boolean completed;
}
