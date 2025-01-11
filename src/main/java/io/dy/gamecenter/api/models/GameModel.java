package io.dy.gamecenter.api.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "games")
@Data
public class GameModel extends TimeModel {

    @Id
    private String id;

    private String name;

    @Field("name_router")
    private String nameRouter;

    //0: Off, 1: On
    private int status;

    @Field("is_display")
    private boolean display;

    @Field("category_ids")
    private List<String> categoryIds;

    private String url;

    private String key;

    private String orientation;

    private String ratio;

    private ImageGameModel image;

    private BannerGameModel banner;

    private String description;

    @Field("description_sub")
    private String descriptionSub;

    @Field("play_type")
    private int playType;

    @Field("platform_type")
    private int platformType;

    @Field("mobile_type")
    private int mobileType;

    private int app;

    @Field("app_url")
    private String appUrl;

    @Field("app_download")
    private AppDownloadGameModel appDownload;

    @Field("game_metadata")
    private GameMetadataModel metadata;

    private int priority;
}
