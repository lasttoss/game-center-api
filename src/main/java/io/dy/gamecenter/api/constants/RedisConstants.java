package io.dy.gamecenter.api.constants;

public class RedisConstants {

    public final static String LEADERBOARD_RANK = "leaderboard:rank:";

    public final static String LEADERBOARD_INFO = "leaderboard:info";

    public final static long DAILY_LEADERBOARD_EXPIRED = 7 * 24 * 60 * 60;

    public final static long WEEKLY_LEADERBOARD_EXPIRED = 14 * 24 * 60 * 60;

    public final static long MONTHLY_LEADERBOARD_EXPIRED = 60 * 24 * 60 * 60;
}
