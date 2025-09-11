package com.umc.gusto.domain.store.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SearchStoreResponse {
    List<GetStoreInfoResponse> stores;
    boolean hasNext;
    Long cursorId;

    public static SearchStoreResponse of(List<GetStoreInfoResponse> stores, boolean hasNext, Long cursorId) {
        return SearchStoreResponse.builder()
                .stores(stores)
                .hasNext(hasNext)
                .cursorId(cursorId)
                .build();
    }
}
