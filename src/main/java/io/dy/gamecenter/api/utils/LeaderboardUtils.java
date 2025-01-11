package io.dy.gamecenter.api.utils;

import io.dy.gamecenter.api.constants.Enums;
import io.dy.gamecenter.api.constants.RedisConstants;
import org.joda.time.LocalDate;

public class LeaderboardUtils {

    public static String getKey(String gameId, String prefixKey, int type) {
        LocalDate today = LocalDate.now();
        String fullDate = "";
        String subfixKey = "";

        if (type == Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue()) {
            fullDate = today.getYear() + "-" + today.getMonthOfYear() + "-" + today.getDayOfMonth();
            subfixKey = "day";
        }

        if (type == Enums.LeaderboardRankTypeEnum.WEEKLY_LEADERBOARD.getValue()) {
            fullDate = today.getYear() + "-" + today.getWeekOfWeekyear();
            subfixKey = "week";
        }

        if (type == Enums.LeaderboardRankTypeEnum.MONTHLY_LEADERBOARD.getValue()) {
            fullDate = today.getYear() + "-" + today.getMonthOfYear();
            subfixKey = "month";
        }
        return RedisConstants.LEADERBOARD_RANK + prefixKey + ":" + subfixKey + ":" + fullDate + ":" + gameId;
    }
}
