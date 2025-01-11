package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.dto.requests.PagingRequest;
import io.dy.gamecenter.api.dto.responses.CategoryResponseData;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.CategoryModel;
import io.dy.gamecenter.api.repositories.CategoryRepository;
import io.dy.gamecenter.api.utils.ModelMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Response listByPaging(PagingRequest request) {
        Response response = new Response();
        Page<CategoryModel> items = categoryRepository.findByPage(request);
        Page<CategoryResponseData> data = items.map(item -> ModelMapperUtils.map(item, CategoryResponseData.class));
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        return response;
    }

    public Response getItemById(String id) {
        Response response = new Response();
        CategoryModel item = categoryRepository.findById(id);
        CategoryResponseData data = ModelMapperUtils.map(item, CategoryResponseData.class);
        response.setData(data);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
}
