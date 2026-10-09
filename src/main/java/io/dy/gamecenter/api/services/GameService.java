package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.dto.requests.PagingRequest;
import io.dy.gamecenter.api.dto.responses.GameResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.GameModel;
import io.dy.gamecenter.api.repositories.GameRepository;
import io.dy.gamecenter.api.utils.ModelMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public Response listByPaging(PagingRequest request) {
        Response response = new Response();
        List<GameResponseData> listData = new ArrayList<>();
        List<GameModel> items = gameRepository.findAll(request);
        for (GameModel item : items) {
            GameResponseData i = ModelMapperUtils.map(item, GameResponseData.class);
            listData.add(i);
        }
        Collections.sort(listData, new GameResponseData());
        long total = gameRepository.countTotal();
        Page<GameResponseData> data = new PageImpl<>(listData, PageRequest.of(request.getOffset(), request.getLimit()), total);
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        return response;
    }

    public Response getItemById(String id) {
        Response response = new Response();
        GameModel item = gameRepository.findById(id);
        GameResponseData data = ModelMapperUtils.map(item, GameResponseData.class);
        response.setData(data);
        // Was setData here: the answer to "give me this game" was the number 200 in the data field
        // and a status of zero, so a client got no game at all.
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    public Response getItemByNameRouter(String nameRouter) {
        Response response = new Response();
        GameModel item = gameRepository.findByNameRouter(nameRouter);
        if (item != null) {
            GameResponseData data = ModelMapperUtils.map(item, GameResponseData.class);
            response.setData(data);
        }
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
}
