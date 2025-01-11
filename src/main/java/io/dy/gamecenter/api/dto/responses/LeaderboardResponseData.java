package io.dy.gamecenter.api.dto.responses;

import io.dy.gamecenter.api.models.UserMetadataModel;
import lombok.Data;

@Data
public class LeaderboardResponseData {

    private int score;

    private UserMetadataModel metadata;
}
