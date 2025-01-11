package io.dy.gamecenter.api.repositories;

import io.dy.gamecenter.api.models.MatchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MatchRepository {

    @Autowired
    @Qualifier("mongoDbTemplate")
    private MongoTemplate mongoTemplate;

    public MatchModel findByMatchId(String matchId) {
        Query query = new Query().addCriteria(Criteria.where("match_id").is(matchId));
        return mongoTemplate.findOne(query, MatchModel.class);
    }

    public MatchModel save(MatchModel item) {
        return mongoTemplate.save(item);
    }
}
