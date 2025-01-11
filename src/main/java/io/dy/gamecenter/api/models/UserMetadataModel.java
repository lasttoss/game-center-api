package io.dy.gamecenter.api.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class UserMetadataModel {

    @Field("display_name")
    private String displayName;

    @Field("avatar_url")
    private String avatarUrl;

    public UserMetadataModel() {
    }

    public UserMetadataModel(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }
}
