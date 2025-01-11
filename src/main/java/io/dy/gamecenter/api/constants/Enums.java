package io.dy.gamecenter.api.constants;

import lombok.Getter;

public class Enums {

    @Getter
    public enum SocialTypeLogin {
        AUTH_LOGIN(0), FACEBOOK_LOGIN(1), GOOGLE_LOGIN(2), APPLE_LOGIN(3), DEVICE_LOGIN(4);

        private int value;

        SocialTypeLogin(int value) {
            this.value = value;
        }
    }

    @Getter
    public enum SortingEnum {
        ASC(1), DESC(-1);

        private int value;

        SortingEnum(int value) {
            this.value = value;
        }
    }

    @Getter
    public enum StatusEnum {
        OFF(0), ON(1);

        private int value;

        StatusEnum(int value) {
            this.value = value;
        }
    }

    @Getter
    public enum LeaderboardRankTypeEnum {
        DAILY_LEADERBOARD(0), WEEKLY_LEADERBOARD(1), MONTHLY_LEADERBOARD(2);

        private int value;

        LeaderboardRankTypeEnum(int value) {
            this.value = value;
        }
    }

    @Getter
    public enum LeaderboardPrefixKeyEnum {
        RANK("rank"), UPDATE("update");

        private String value;

        LeaderboardPrefixKeyEnum(String value) {
            this.value = value;
        }
    }
}
