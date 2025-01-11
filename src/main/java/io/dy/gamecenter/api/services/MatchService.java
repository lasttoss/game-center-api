package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.ApiErrorEnum;
import io.dy.gamecenter.api.dto.requests.MatchRequest;
import io.dy.gamecenter.api.dto.responses.ErrorDTO;
import io.dy.gamecenter.api.dto.responses.MatchResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.GameModel;
import io.dy.gamecenter.api.models.MatchModel;
import io.dy.gamecenter.api.repositories.GameRepository;
import io.dy.gamecenter.api.repositories.MatchRepository;
import io.dy.gamecenter.api.utils.ModelMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    public Response create(String userId, String gameId, String gameKey) {
        Response response = new Response();
        GameModel game = gameRepository.findById(gameId);
        if (game == null || !game.getKey().equals(gameKey)) {
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.ITEM_NOT_FOUND);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }
        MatchModel item = new MatchModel(gameId, userId);
        item = matchRepository.save(item);
        MatchResponseData data = ModelMapperUtils.map(item, MatchResponseData.class);
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        return response;
    }

    public Response update(String matchId, MatchRequest request, String userId, String gameId, String gameKey) {
        Response response = new Response();

        GameModel game = gameRepository.findById(gameId);
        if (game == null || !game.getKey().equals(gameKey)) {
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.ITEM_NOT_FOUND);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        MatchModel item = matchRepository.findByMatchId(matchId);
        if (item == null) {
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.ITEM_NOT_FOUND);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        if (!item.getUserId().equals(userId)) {
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.MATCH_NOT_FOUND);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        if (item.isCompleted()) {
            response.setStatus(HttpStatus.CONFLICT.value());
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.CAN_NOT_UPDATE_THIS_MATCH);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        item.updateScore(request.getScore());
        item = matchRepository.save(item);
        MatchResponseData data = ModelMapperUtils.map(item, MatchResponseData.class);
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        leaderboardService.save(gameId, userId, request.getScore());
        return response;
    }
}
