package io.dy.gamecenter.api.models;

import lombok.Data;
import org.hashids.Hashids;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "matches")
@Data
public class MatchModel extends TimeModel{

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("match_id")
    private String matchId;

    @Field("game_id")
    private String gameId;

    private int score;

    @Field("is_completed")
    private boolean completed;

    public MatchModel() {}

    public MatchModel(String gameId, String userId) {
        Hashids hashids = new Hashids(UUID.randomUUID().toString());
        String matchId = hashids.encode(1L, 1L, 1L, 1L, 1L, 1L);
        this.matchId = matchId;
        this.gameId = gameId;
        this.userId = userId;
        this.score = 0;
        this.completed = false;
        this.setCreatedAt(DateTime.now().toDate());
        this.setUpdatedAt(DateTime.now().toDate());
    }

    public void updateScore(int score) {
        this.score = score;
        this.completed = true;
        this.setUpdatedAt(DateTime.now().toDate());
    }
}
