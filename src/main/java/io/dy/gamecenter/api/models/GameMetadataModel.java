package io.dy.gamecenter.api.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class GameMetadataModel implements Serializable {

    private String sdkVersion;

    private int maxScorePerSecond;

    private int type;
}
