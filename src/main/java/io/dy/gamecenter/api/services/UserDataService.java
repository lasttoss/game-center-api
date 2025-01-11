package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.ApiErrorEnum;
import io.dy.gamecenter.api.dto.requests.UserDataRequest;
import io.dy.gamecenter.api.dto.responses.ErrorDTO;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.UserDataModel;
import io.dy.gamecenter.api.repositories.UserDataRepository;
import io.dy.gamecenter.api.utils.GzipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserDataService {

    @Autowired
    private UserDataRepository userDataRepository;

    public Response save(String userId, UserDataRequest request) {
        Response response = new Response();

        UserDataModel item = userDataRepository.findByUserId(userId);
        String compressedData = "";
        try {
            compressedData = GzipUtils.compress(request.getData());
        } catch (IOException e) {
            ErrorDTO error = new ErrorDTO(ApiErrorEnum.CAN_NOT_UPDATE_DATA);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setError(error);
            return response;
        }

        if (item == null) {
            item = new UserDataModel(userId, compressedData);
        } else {
            item.setData(compressedData);
        }
        userDataRepository.save(item);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
}
