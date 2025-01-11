package io.dy.gamecenter.api.dto.responses;

import io.dy.gamecenter.api.models.CategoryImageModel;
import lombok.Data;

import java.util.List;

@Data
public class CategoryResponseData {

    private String id;

    private String name;

    private CategoryImageModel image;

    private List<GameResponseData> games;
}
