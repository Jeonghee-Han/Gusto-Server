package com.umc.gusto.domain.group.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PagingResponse {
    List<?> result;
    boolean hasNext;
}
