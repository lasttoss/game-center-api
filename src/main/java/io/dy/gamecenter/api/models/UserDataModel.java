package io.dy.gamecenter.api.models;

import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user_data")
@Data
public class UserDataModel extends TimeModel {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    private String data;

    public UserDataModel () {}

    public UserDataModel(String userId, String data) {
        this.userId = userId;
        this.data = data;
        this.setCreatedAt(DateTime.now().toDate());
        this.setUpdatedAt(DateTime.now().toDate());
    }
}
