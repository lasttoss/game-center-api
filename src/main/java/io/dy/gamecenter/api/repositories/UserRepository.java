package io.dy.gamecenter.api.repositories;

import io.dy.gamecenter.api.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


@Repository
public class UserRepository {

    @Autowired
    @Qualifier("mongoDbTemplate")
    private MongoTemplate mongoTemplate;

    public UserModel findByUserId(String userId) {
        Query query = new Query().addCriteria(Criteria.where("user_id").is(userId));
        return mongoTemplate.findOne(query, UserModel.class);
    }

    public UserModel findByUsername(String username) {
        Query query = new Query().addCriteria(Criteria.where("username").is(username));
        return mongoTemplate.findOne(query, UserModel.class);
    }

    public UserModel save(UserModel item) {
        return mongoTemplate.save(item);
    }
}
