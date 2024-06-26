package com.umc.gusto.domain.route.model.response;

import lombok.*;

import java.util.List;


@Builder
@Getter
@AllArgsConstructor
public class RoutePagingResponse {
    private Boolean hasNext;
    private List<?> result;
}
