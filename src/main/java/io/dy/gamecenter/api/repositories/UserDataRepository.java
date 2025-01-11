package io.dy.gamecenter.api.repositories;

import io.dy.gamecenter.api.models.LeaderboardModel;
import io.dy.gamecenter.api.models.UserDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UserDataRepository {

    @Autowired
    @Qualifier("mongoDbTemplate")
    private MongoTemplate mongoTemplate;

    public void save(UserDataModel item) {
        mongoTemplate.save(item);
    }

    public UserDataModel findByUserId(String userId) {
        Query query = new Query().addCriteria(Criteria.where("user_id").is(userId));
        return mongoTemplate.findOne(query, UserDataModel.class);
    }
}
