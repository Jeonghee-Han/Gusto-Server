package com.umc.gusto.domain.myCategory.service;

import com.umc.gusto.domain.myCategory.entity.Pin;
import com.umc.gusto.domain.myCategory.model.request.CreatePinRequest;
import com.umc.gusto.domain.myCategory.model.request.UpdatePinRequest;
import com.umc.gusto.domain.myCategory.model.response.CreatePinResponse;
import com.umc.gusto.domain.user.entity.User;

import java.util.List;

public interface PinService {
    CreatePinResponse createPin(User user, Long myCategoryId, CreatePinRequest createPin);
    void deletePin(User user, List<Long> pinIds);
    void updatePin(User user, List<Long> pinIds, UpdatePinRequest request);
}
