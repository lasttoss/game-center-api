package io.dy.gamecenter.api.repositories;

import com.google.gson.Gson;
import io.dy.gamecenter.api.constants.Enums;
import io.dy.gamecenter.api.dto.requests.PagingRequest;
import io.dy.gamecenter.api.models.GameModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GameRepository {

    @Autowired
    @Qualifier("mongoDbTemplate")
    private MongoTemplate mongoTemplate;

    public List<GameModel> findAll(PagingRequest request) {
        Query query = new Query().addCriteria(Criteria.where("status").is(Enums.StatusEnum.ON.getValue()));
        List<GameModel> items = null;
        Pageable paging = request.getSort() == Enums.SortingEnum.DESC.getValue() ? PageRequest.of(request.getOffset(), request.getLimit(), Sort.by("id").descending()) : PageRequest.of(request.getOffset(), request.getLimit(), Sort.by("id").ascending());
        query.with(paging);
        items = mongoTemplate.find(query, GameModel.class);
        return items;
    }

    public long countTotal() {
        Query query = new Query().addCriteria(Criteria.where("status").is(Enums.StatusEnum.ON.getValue()));
        long total = 0;
        total = mongoTemplate.count(query, GameModel.class);
        return total;
    }

    public List<GameModel> listOpen() {
        Query query = new Query().addCriteria(Criteria.where("status").is(Enums.StatusEnum.ON.getValue()));
        return mongoTemplate.find(query, GameModel.class);
    }

    public GameModel findByNameRouter(String nameRouter) {
        Query query = new Query().addCriteria(Criteria.where("name_router").is(nameRouter));
        GameModel item = mongoTemplate.findOne(query, GameModel.class);
        return item;
    }

    public GameModel findById(String id) {
        Query query = new Query().addCriteria(Criteria.where("id").is(id));
        GameModel item = mongoTemplate.findOne(query, GameModel.class);
        return item;
    }
}
