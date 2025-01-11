package io.dy.gamecenter.api.dto.responses;

import io.dy.gamecenter.api.models.AppDownloadGameModel;
import io.dy.gamecenter.api.models.BannerGameModel;
import io.dy.gamecenter.api.models.CategoryModel;
import io.dy.gamecenter.api.models.ImageGameModel;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
public class GameResponseData implements Comparator<GameResponseData> {

    private String id;

    private String name;

    private String nameRouter;

    private List<CategoryModel> categoryIds;

    private String url;

    private String orientation;

    private String ratio;

    private ImageGameModel image;

    private BannerGameModel banner;

    private int playType;

    private int platformType;

    private int leaderboardType;

    private Date createdAt;

    private Date updatedAt;

    private int mobileType;

    private int app;

    private String appUrl;

    private AppDownloadGameModel appDownload;

    private boolean isGameOfDay = false;

    private boolean isDailyLeaderboardEvent = false;

    private boolean hasQuest = false;

    private int priority;

    @Override
    public int compare(GameResponseData o1, GameResponseData o2) {
        return new CompareToBuilder().append(o2.isGameOfDay(), o1.isGameOfDay())
                .append(o1.priority, o2.priority)
                .append(o2.isDailyLeaderboardEvent(), o1.isDailyLeaderboardEvent())
                .append(o2.isHasQuest(), o1.isHasQuest())
                .build();
    }
}
