package io.dy.gamecenter.api.dto.requests;

import lombok.Data;

@Data
public class PagingRequest {

    private int limit;

    private int offset;

    //0: ASC, 1: DESC
    private int sort;

    @Override
    public String toString() {
        return limit + ":" + offset + ":" + sort;
    }
}
