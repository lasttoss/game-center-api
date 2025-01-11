package io.dy.gamecenter.api.dto.responses;

import lombok.Data;

@Data
public class RatingGameResponseData {

    private String gameId;

    private int totalRatingUsers;

    private int totalRating;

    private double averageRating;
}
