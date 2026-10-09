package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.dto.requests.PagingRequest;
import io.dy.gamecenter.api.dto.responses.GameResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.GameModel;
import io.dy.gamecenter.api.repositories.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * The game service is what the client sees when it opens the game list, so these tests are about the
 * shape of the answer: the page size and offset the caller asked for, the total the database reports,
 * and the order the items come back in.
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    GameRepository gameRepository;

    @InjectMocks
    GameService gameService;

    static GameModel game(String id, String name) {
        GameModel model = new GameModel();
        model.setId(id);
        model.setName(name);
        model.setNameRouter(name.toLowerCase().replace(' ', '-'));
        return model;
    }

    static PagingRequest paging(int limit, int offset) {
        PagingRequest request = new PagingRequest();
        request.setLimit(limit);
        request.setOffset(offset);
        return request;
    }

    @Test
    void listByPagingAnswersWithThePageTheCallerAskedFor() {
        when(gameRepository.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(game("g3", "Charlie"), game("g1", "Alpha"), game("g2", "Bravo")));
        when(gameRepository.countTotal()).thenReturn(37L);

        Response response = gameService.listByPaging(paging(10, 2));

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());

        @SuppressWarnings("unchecked")
        Page<GameResponseData> page = (Page<GameResponseData>) response.getData();
        assertEquals(37L, page.getTotalElements(), "the total is what the caller uses to draw the pager");
        assertEquals(10, page.getSize());
        assertEquals(2, page.getNumber(), "the offset the caller sent is the page number");

        List<String> answered = new ArrayList<>();
        page.getContent().forEach(item -> answered.add(item.getId()));
        assertEquals(3, answered.size());

        List<GameResponseData> expected = new ArrayList<>();
        List.of(game("g3", "Charlie"), game("g1", "Alpha"), game("g2", "Bravo")).forEach(model -> {
            GameResponseData data = new GameResponseData();
            data.setId(model.getId());
            expected.add(data);
        });
        Collections.sort(expected, new GameResponseData());
        List<String> inOrder = new ArrayList<>();
        expected.forEach(item -> inOrder.add(item.getId()));

        assertEquals(inOrder, answered, "the answer is sorted; the order of the database is not the order of the page");
    }

    /**
     * This is a copy-paste bug, written down rather than quietly fixed: the second call in the method
     * is setData where it means setStatus, so the answer to "give me this game" is the number 200 in
     * the data field and a status of zero. A client reading it gets no game at all.
     */
    @Test
    void getItemByIdAnswersWithTheGame() {
        when(gameRepository.findById("g1")).thenReturn(game("g1", "Alpha"));

        Response response = gameService.getItemById("g1");

        assertEquals(HttpStatus.OK.value(), response.getStatus(), "the status was never set");
        GameResponseData data = (GameResponseData) response.getData();
        assertNotNull(data, "the data field holds the status code instead of the game");
        assertEquals("g1", data.getId());
    }

    @Test
    void getItemByNameRouterAnswersWithTheGame() {
        when(gameRepository.findByNameRouter("alpha")).thenReturn(game("g1", "Alpha"));

        Response response = gameService.getItemByNameRouter("alpha");

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("g1", ((GameResponseData) response.getData()).getId());
    }

    @Test
    void getItemByNameRouterAnswersWithNothingWhenThereIsNoSuchRoute() {
        when(gameRepository.findByNameRouter("nope")).thenReturn(null);

        Response response = gameService.getItemByNameRouter("nope");

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(response.getData(), "a game that does not exist is answered with an empty body, not an error");
    }
}
