package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.Enums;
import io.dy.gamecenter.api.constants.RedisConstants;
import io.dy.gamecenter.api.dto.responses.LeaderboardResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.LeaderboardModel;
import io.dy.gamecenter.api.models.UserMetadataModel;
import io.dy.gamecenter.api.models.UserModel;
import io.dy.gamecenter.api.repositories.LeaderboardRepository;
import io.dy.gamecenter.api.repositories.UserRepository;
import io.dy.gamecenter.api.utils.LeaderboardUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A leaderboard is three caches and a database row per period, so these tests are about the keys and
 * the durations: a daily entry that expires with the weekly one is a leaderboard that silently stops
 * making sense tomorrow.
 *
 * The keys are compared against LeaderboardUtils rather than typed out, so a change to the key format
 * fails here once instead of every test separately.
 */
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    private static final String GAME_ID = "cozy-garden";
    private static final String USER_ID = "user-1";

    @Mock
    LeaderboardRepository leaderboardRepository;

    @Mock
    RedisService redisService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    LeaderboardService leaderboardService;

    static String rankKey(int type) {
        return LeaderboardUtils.getKey(GAME_ID, Enums.LeaderboardPrefixKeyEnum.RANK.getValue(), type);
    }

    static String updateKey(int type) {
        return LeaderboardUtils.getKey(GAME_ID, Enums.LeaderboardPrefixKeyEnum.UPDATE.getValue(), type);
    }

    static UserModel user() {
        UserModel user = new UserModel("player", "irrelevant");
        user.setUserId(USER_ID);
        user.setAvatarUrl("https://assets.test/avatar.png");
        return user;
    }

    static ZSetOperations.TypedTuple<String> entry(String userId, double score) {
        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        when(tuple.getValue()).thenReturn(userId);
        when(tuple.getScore()).thenReturn(score);
        return tuple;
    }

    @Test
    void saveWritesTheScoreToAllThreePeriods() {
        when(redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, USER_ID)).thenReturn(true);
        when(redisService.getObject(RedisConstants.LEADERBOARD_INFO, USER_ID))
                .thenReturn(new UserMetadataModel("player", "https://assets.test/avatar.png"));
        when(leaderboardRepository.findByKeyAndUserId(anyString(), eq(USER_ID))).thenReturn(null);

        leaderboardService.save(GAME_ID, USER_ID, 42);

        int[] periods = {
                Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue(),
                Enums.LeaderboardRankTypeEnum.WEEKLY_LEADERBOARD.getValue(),
                Enums.LeaderboardRankTypeEnum.MONTHLY_LEADERBOARD.getValue(),
        };
        for (int type : periods) {
            verify(redisService).saveLeaderboard(eq(rankKey(type)), eq(USER_ID), eq(42), any(Duration.class));
            verify(redisService).saveObjectWithExpiredTime(eq(updateKey(type)), eq(USER_ID), anyLong(), any(Duration.class));
        }
        verify(leaderboardRepository, times(3)).save(any());

        // The cached metadata was there, so the database was not asked about this player again.
        verify(userRepository, never()).findByUserId(anyString());
    }

    @Test
    void saveFetchesThePlayerOnceAndCachesItForTheLeaderboard() {
        when(redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, USER_ID)).thenReturn(false);
        when(userRepository.findByUserId(USER_ID)).thenReturn(user());
        when(leaderboardRepository.findByKeyAndUserId(anyString(), eq(USER_ID))).thenReturn(null);

        leaderboardService.save(GAME_ID, USER_ID, 7);

        ArgumentCaptor<UserMetadataModel> cached = ArgumentCaptor.forClass(UserMetadataModel.class);
        verify(redisService).saveObjectWithoutExpiredTime(eq(RedisConstants.LEADERBOARD_INFO), eq(USER_ID), cached.capture());
        assertEquals("https://assets.test/avatar.png", cached.getValue().getAvatarUrl(),
                "the leaderboard shows the avatar, so it comes from the user record");
        verify(userRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void saveUpdatesTheRowThatIsAlreadyThereInsteadOfReplacingIt() {
        when(redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, USER_ID)).thenReturn(true);
        when(redisService.getObject(RedisConstants.LEADERBOARD_INFO, USER_ID))
                .thenReturn(new UserMetadataModel("player", "https://assets.test/avatar.png"));
        LeaderboardModel existing = new LeaderboardModel(rankKey(Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue()),
                USER_ID, 10, new UserMetadataModel("player", ""), 0);
        when(leaderboardRepository.findByKeyAndUserId(anyString(), eq(USER_ID))).thenReturn(existing);

        leaderboardService.save(GAME_ID, USER_ID, 99);

        assertEquals(99, existing.getScore(), "the row on record did not take the new score");
        assertTrue(existing.getTimestamp() > 0, "the update time was not refreshed");
    }

    @Test
    void listAnswersWithThePlayersOnTheBoard() {
        // Built before the stub: Mockito does not allow stubbing one mock inside another's arguments.
        ZSetOperations.TypedTuple<String> tuple = entry(USER_ID, 42.4);
        when(redisService.getLeaderboard(rankKey(Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue()), 0, 50))
                .thenReturn(entries(tuple));
        when(redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, USER_ID)).thenReturn(true);
        when(redisService.getObject(RedisConstants.LEADERBOARD_INFO, USER_ID))
                .thenReturn(new UserMetadataModel("player", "https://assets.test/avatar.png"));
        when(redisService.checkIfKeyExists(updateKey(Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue()), USER_ID)).thenReturn(true);
        when(redisService.getObject(updateKey(Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue()), USER_ID)).thenReturn(1_700_000_000);

        Response response = leaderboardService.list(GAME_ID, Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue());

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<LeaderboardResponseData> data = (List<LeaderboardResponseData>) response.getData();
        assertEquals(1, data.size());
        assertEquals(42, data.get(0).getScore(), "a fractional score is rounded, not truncated");
    }

    @Test
    void listCachesTheMetadataOfAPlayerItHasNotSeen() {
        ZSetOperations.TypedTuple<String> tuple = entry(USER_ID, 10.0);
        when(redisService.getLeaderboard(anyString(), anyInt(), anyInt())).thenReturn(entries(tuple));
        when(redisService.checkIfKeyExists(RedisConstants.LEADERBOARD_INFO, USER_ID)).thenReturn(false);
        when(userRepository.findByUserId(USER_ID)).thenReturn(user());

        Response response = leaderboardService.list(GAME_ID, Enums.LeaderboardRankTypeEnum.WEEKLY_LEADERBOARD.getValue());

        assertNotNull(response.getData());
        verify(redisService).saveObjectWithoutExpiredTime(eq(RedisConstants.LEADERBOARD_INFO), eq(USER_ID), any());
    }

    @Test
    void listOfAnEmptyBoardIsAnEmptyList() {
        when(redisService.getLeaderboard(anyString(), anyInt(), anyInt())).thenReturn(entries());

        Response response = leaderboardService.list(GAME_ID, Enums.LeaderboardRankTypeEnum.DAILY_LEADERBOARD.getValue());

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<LeaderboardResponseData> data = (List<LeaderboardResponseData>) response.getData();
        assertEquals(0, data.size());
        verify(userRepository, never()).findByUserId(anyString());
    }

    @SafeVarargs
    static Set<ZSetOperations.TypedTuple<String>> entries(ZSetOperations.TypedTuple<String>... tuples) {
        return new LinkedHashSet<>(List.of(tuples));
    }
}
