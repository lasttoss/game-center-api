package io.dy.gamecenter.api.repositories;

import io.dy.gamecenter.api.constants.Enums;
import io.dy.gamecenter.api.dto.requests.PagingRequest;
import io.dy.gamecenter.api.models.CategoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {

    @Autowired
    @Qualifier("mongoDbTemplate")
    private MongoTemplate mongoTemplate;

    public Page<CategoryModel> findByPage(PagingRequest request) {
        Query query = new Query();
        query.addCriteria(Criteria.where("display").is(true));
        long total = mongoTemplate.count(query, CategoryModel.class);
        Pageable paging = request.getSort() == Enums.SortingEnum.DESC.getValue() ?
                PageRequest.of(request.getOffset(), request.getLimit(), Sort.by("id").descending()) :
                PageRequest.of(request.getOffset(), request.getLimit(), Sort.by("id").ascending());
        query.with(paging);
        List<CategoryModel> items = mongoTemplate.find(query, CategoryModel.class);
        return new PageImpl<>(items, PageRequest.of(request.getOffset(), request.getLimit()), total);
    }

    public CategoryModel findById(String id) {
        Query query = new Query().addCriteria(Criteria.where("id").is(id));
        CategoryModel item = mongoTemplate.findOne(query, CategoryModel.class);
        return item;
    }

    public List<CategoryModel> listAll() {
        Query query = new Query();
        query.addCriteria(Criteria.where("display").is(true));
        return mongoTemplate.find(query, CategoryModel.class);
    }
}
