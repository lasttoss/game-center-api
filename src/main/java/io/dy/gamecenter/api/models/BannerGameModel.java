package io.dy.gamecenter.api.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class BannerGameModel {

    private boolean hot;

    @Field("new")
    private boolean isNew;

    private boolean popular;

    private boolean quest;
}
