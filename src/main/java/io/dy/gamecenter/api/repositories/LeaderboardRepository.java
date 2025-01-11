package io.dy.gamecenter.api.repositories;

import io.dy.gamecenter.api.models.LeaderboardModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class LeaderboardRepository {

    @Autowired
    @Qualifier("mongoDbTemplate")
    private MongoTemplate mongoTemplate;

    public void save(LeaderboardModel item) {
        mongoTemplate.save(item);
    }

    public LeaderboardModel findByKeyAndUserId(String key, String userId) {
        Query query = new Query().addCriteria(Criteria.where("key").is(key).and("user_id").is(userId));
        return mongoTemplate.findOne(query, LeaderboardModel.class);
    }
}
