package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.Enums;
import io.dy.gamecenter.api.constants.RedisConstants;
import io.dy.gamecenter.api.dto.requests.MatchRequest;
import io.dy.gamecenter.api.dto.responses.LeaderboardResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.LeaderboardModel;
import io.dy.gamecenter.api.models.UserMetadataModel;
import io.dy.gamecenter.api.models.UserModel;
import io.dy.gamecenter.api.repositories.LeaderboardRepository;
import io.dy.gamecenter.api.repositories.UserRepository;
import io.dy.gamecenter.api.utils.LeaderboardUtils;
import io.dy.gamecenter.api.utils.ModelMapperUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserRepository userRepository;


    public Response list(String gameId, int type) {
        Response response = new Response();

        String rankKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.RANK.getValue(), type);
        String updateKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.UPDATE.getValue(), type);
        List<LeaderboardModel> items = redisService.getLeaderboard(rankKey, 0, 50).stream().map(e -> {
            UserMetadataModel metadata;
            if (redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, e.getValue())) {
                metadata = (UserMetadataModel) redisService.getObject(RedisConstants.LEADERBOARD_INFO, e.getValue());
            } else {
                UserModel user = userRepository.findByUserId(e.getValue());
                metadata = new UserMetadataModel(user.getDisplayName(), user.getAvatarUrl());
                redisService.saveObjectWithoutExpiredTime(RedisConstants.LEADERBOARD_INFO, e.getValue(), metadata);
            }
            long currentTimestamp = redisService.checkIfKeyExists(updateKey, e.getValue()) ? (int) redisService.getObject(updateKey, e.getValue()) : 0;
            LeaderboardModel item = new LeaderboardModel(rankKey, e.getValue(), (int) Math.round(e.getScore()), metadata, currentTimestamp);
            return item;
        }).collect(Collectors.toList());
        Collections.sort(items, new LeaderboardModel());
        List<LeaderboardResponseData> data = ModelMapperUtils.mapAll(items, LeaderboardResponseData.class);

        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        return response;
    }

    public void save(String gameId, String userId, int score) {
        UserMetadataModel metadata;
        if (redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, userId)) {
            metadata = (UserMetadataModel) redisService.getObject(RedisConstants.LEADERBOARD_INFO, userId);
        } else {
            UserModel user = userRepository.findByUserId(userId);
            metadata = new UserMetadataModel(user.getDisplayName(), user.getAvatarUrl());
            redisService.saveObjectWithoutExpiredTime(RedisConstants.LEADERBOARD_INFO, userId, metadata);
        }

        // save daily rank
        String dailyRankKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.RANK.getValue(), Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue());
        String dailyUpdateKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.UPDATE.getValue(), Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue());
        saveCachedLeaderboard(userId, dailyRankKey, dailyUpdateKey, score, metadata, RedisConstants.DAILY_LEADERBOARD_EXPIRED);

        // save weekly rank
        String weeklyRankKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.RANK.getValue(), Enums.LeaderboardRankTypeEnum.WEEKLY_LEADERBOARD.getValue());
        String weeklyUpdateKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.UPDATE.getValue(), Enums.LeaderboardRankTypeEnum.WEEKLY_LEADERBOARD.getValue());
        saveCachedLeaderboard(userId, weeklyRankKey, weeklyUpdateKey, score, metadata, RedisConstants.WEEKLY_LEADERBOARD_EXPIRED);

        // save monthly rank
        String monthlyRankKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.RANK.getValue(), Enums.LeaderboardRankTypeEnum.MONTHLY_LEADERBOARD.getValue());
        String monthlyUpdateKey = LeaderboardUtils.getKey(gameId, Enums.LeaderboardPrefixKeyEnum.UPDATE.getValue(), Enums.LeaderboardRankTypeEnum.MONTHLY_LEADERBOARD.getValue());
        saveCachedLeaderboard(userId, monthlyRankKey, monthlyUpdateKey, score, metadata, RedisConstants.MONTHLY_LEADERBOARD_EXPIRED);
    }

    private void saveCachedLeaderboard(String userId, String rankKey, String updateKey, int score, UserMetadataModel metadata, long expiredTime) {
        long currentTimestamp = DateTime.now().getMillis() / 1000;
        redisService.saveLeaderboard(rankKey, userId, score, Duration.ofSeconds(expiredTime));
        redisService.saveObjectWithExpiredTime(updateKey, userId, DateTime.now().getMillis() / 1000, Duration.ofSeconds(expiredTime));
        LeaderboardModel item = leaderboardRepository.findByKeyAndUserId(rankKey, userId);
        if (item == null) {
            item = new LeaderboardModel(rankKey, userId, score, metadata, currentTimestamp);
        } else {
            item.update(score, metadata, currentTimestamp);
        }
        leaderboardRepository.save(item);
    }
}
