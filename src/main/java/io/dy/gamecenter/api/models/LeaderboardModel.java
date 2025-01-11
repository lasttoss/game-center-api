package io.dy.gamecenter.api.models;

import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Comparator;

@Document(collection = "leaderboards")
@Data
public class LeaderboardModel extends TimeModel implements Comparator<LeaderboardModel> {

    @Id
    private String id;

    private String key;

    private int score;

    @Field("user_id")
    private String userId;

    private UserMetadataModel metadata;

    private long timestamp;

    public LeaderboardModel() {}

    public LeaderboardModel(String key, String userId, int score, UserMetadataModel metadata, long timestamp) {
        this.key = key;
        this.userId = userId;
        this.score = score;
        this.metadata = metadata;
        this.timestamp = timestamp;
        this.setCreatedAt(DateTime.now().toDate());
        this.setUpdatedAt(DateTime.now().toDate());
    }

    @Override
    public int compare(LeaderboardModel o1, LeaderboardModel o2) {
        return new CompareToBuilder().append(o2.getScore(), o1.getScore()).append(o1.getTimestamp(), o2.getTimestamp()).build();
    }

    public void update(int score, UserMetadataModel metadata, long timestamp) {
        this.score = score;
        this.metadata = metadata;
        this.timestamp = timestamp;
        this.setUpdatedAt(DateTime.now().toDate());
    }
}
