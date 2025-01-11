package io.dy.gamecenter.api.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@Data
public class CategoryModel extends TimeModel {

    @Id
    private String id;

    private String name;

    private boolean display;

    private CategoryImageModel image;
}
