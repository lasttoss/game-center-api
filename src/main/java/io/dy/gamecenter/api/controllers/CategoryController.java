package io.dy.gamecenter.api.controllers;

import io.dy.gamecenter.api.dto.requests.PagingRequest;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping(value = "")
    public ResponseEntity<Response> list(@Valid PagingRequest request) {
        Response response = categoryService.listByPaging(request);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Response> get(@PathVariable String id) {
        Response response = categoryService.getItemById(id);
        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
