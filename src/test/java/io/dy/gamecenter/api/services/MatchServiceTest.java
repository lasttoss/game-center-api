package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.ApiErrorEnum;
import io.dy.gamecenter.api.dto.requests.MatchRequest;
import io.dy.gamecenter.api.dto.responses.MatchResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.GameModel;
import io.dy.gamecenter.api.models.MatchModel;
import io.dy.gamecenter.api.repositories.GameRepository;
import io.dy.gamecenter.api.repositories.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A match is the record of one game session, so these tests are about who is allowed to write to it:
 * the game key has to be right, the match has to belong to the caller, and a finished match is not
 * updated again. What the client receives in each case is part of the contract too, because a client
 * that cannot tell "not yours" from "not found" will show the wrong message.
 */
@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    private static final String GAME_ID = "cozy-garden";
    private static final String GAME_KEY = "the-shared-secret";
    private static final String USER_ID = "user-1";
    private static final String MATCH_ID = "match-1";

    @Mock
    MatchRepository matchRepository;

    @Mock
    GameRepository gameRepository;

    @Mock
    LeaderboardService leaderboardService;

    @InjectMocks
    MatchService matchService;

    static GameModel game() {
        GameModel game = new GameModel();
        game.setId(GAME_ID);
        game.setKey(GAME_KEY);
        return game;
    }

    static MatchModel match(String userId) {
        MatchModel item = new MatchModel(GAME_ID, userId);
        item.setMatchId(MATCH_ID);
        return item;
    }

    static MatchRequest score(int score) {
        MatchRequest request = new MatchRequest();
        request.setScore(score);
        return request;
    }

    static void assertRefused(Response response, ApiErrorEnum error) {
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(error.getCode(), response.getError().getCode());
    }

    @Test
    void createRefusesAGameKeyThatDoesNotMatch() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());

        assertRefused(matchService.create(USER_ID, GAME_ID, "not-the-key"), ApiErrorEnum.ITEM_NOT_FOUND);

        verify(matchRepository, never()).save(any());
    }

    @Test
    void createRefusesAGameThatIsNotThere() {
        when(gameRepository.findById(GAME_ID)).thenReturn(null);

        assertRefused(matchService.create(USER_ID, GAME_ID, GAME_KEY), ApiErrorEnum.ITEM_NOT_FOUND);
    }

    @Test
    void createStartsAMatchThatIsNotFinished() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());
        when(matchRepository.save(any())).thenAnswer(call -> {
            MatchModel saved = call.getArgument(0);
            saved.setMatchId(MATCH_ID);
            return saved;
        });

        Response response = matchService.create(USER_ID, GAME_ID, GAME_KEY);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
        assertNotNull(((MatchResponseData) response.getData()).getMatchId(), "the client needs the id to report a score later");

        ArgumentCaptor<MatchModel> stored = ArgumentCaptor.forClass(MatchModel.class);
        verify(matchRepository).save(stored.capture());
        assertEquals(USER_ID, stored.getValue().getUserId());
        assertEquals(GAME_ID, stored.getValue().getGameId());
        assertEquals(0, stored.getValue().getScore());
        assertFalse(stored.getValue().isCompleted(), "a match is open until it is finished");
    }

    @Test
    void updateRefusesAGameKeyThatDoesNotMatch() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());

        assertRefused(matchService.update(MATCH_ID, score(10), USER_ID, GAME_ID, "not-the-key"), ApiErrorEnum.ITEM_NOT_FOUND);

        verify(matchRepository, never()).save(any());
        verify(leaderboardService, never()).save(anyString(), anyString(), anyInt());
    }

    @Test
    void updateRefusesAMatchThatIsNotThere() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());
        when(matchRepository.findByMatchId(MATCH_ID)).thenReturn(null);

        assertRefused(matchService.update(MATCH_ID, score(10), USER_ID, GAME_ID, GAME_KEY), ApiErrorEnum.ITEM_NOT_FOUND);
    }

    /**
     * Somebody else's match answers MATCH_NOT_FOUND rather than something like "not yours", which is
     * the right way round: a client should not be able to learn that a match id exists by guessing.
     */
    @Test
    void updateRefusesAMatchThatBelongsToAnotherPlayer() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());
        when(matchRepository.findByMatchId(MATCH_ID)).thenReturn(match("somebody-else"));

        assertRefused(matchService.update(MATCH_ID, score(10), USER_ID, GAME_ID, GAME_KEY), ApiErrorEnum.MATCH_NOT_FOUND);

        verify(matchRepository, never()).save(any());
    }

    @Test
    void updateRefusesAMatchThatIsAlreadyFinished() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());
        MatchModel finished = match(USER_ID);
        finished.setCompleted(true);
        when(matchRepository.findByMatchId(MATCH_ID)).thenReturn(finished);

        assertRefused(matchService.update(MATCH_ID, score(10), USER_ID, GAME_ID, GAME_KEY), ApiErrorEnum.CAN_NOT_UPDATE_THIS_MATCH);

        verify(matchRepository, never()).save(any());
    }

    @Test
    void updateStoresTheScoreAndPassesItToTheLeaderboard() {
        when(gameRepository.findById(GAME_ID)).thenReturn(game());
        MatchModel existing = match(USER_ID);
        when(matchRepository.findByMatchId(MATCH_ID)).thenReturn(existing);
        when(matchRepository.save(any())).thenAnswer(call -> call.getArgument(0));

        Response response = matchService.update(MATCH_ID, score(42), USER_ID, GAME_ID, GAME_KEY);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
        assertEquals(42, existing.getScore(), "the score of the match on record was not moved");

        verify(leaderboardService).save(eq(GAME_ID), eq(USER_ID), eq(42));
    }
}
