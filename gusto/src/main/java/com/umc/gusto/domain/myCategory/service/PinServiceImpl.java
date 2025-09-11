package com.umc.gusto.domain.myCategory.service;

import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.umc.gusto.domain.myCategory.entity.MyCategory;
import com.umc.gusto.domain.myCategory.entity.Pin;
import com.umc.gusto.domain.myCategory.model.request.CreatePinRequest;
import com.umc.gusto.domain.myCategory.model.request.UpdatePinRequest;
import com.umc.gusto.domain.myCategory.model.response.CreatePinResponse;
import com.umc.gusto.domain.myCategory.repository.MyCategoryRepository;
import com.umc.gusto.domain.myCategory.repository.PinRepository;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.store.repository.StoreRepository;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PinServiceImpl implements PinService{
    private final PinRepository pinRepository;
    private final MyCategoryRepository myCategoryRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreatePinResponse createPin(User user, Long myCategoryId, CreatePinRequest createPin) {
        MyCategory myCategory = myCategoryRepository.findByUserAndMyCategoryId(user, myCategoryId)
                .orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));
        Store store = storeRepository.findById(createPin.getStoreId())
                        .orElseThrow(() -> new GeneralException(Code.STORE_NOT_FOUND));

        // 해당 사용자가 이미 해당 가게를 찜했는지 확인합니다.
        boolean existingPin = pinRepository.existsByUserAndStoreStoreId(user, store.getStoreId());

        if (existingPin) {
            throw new GeneralException(Code.PIN_ALREADY_EXISTS);
        }

        Pin pin = Pin.builder()
                .user(user)
                .myCategory(myCategory)
                .store(store)
                .build();

        Pin savedPin = pinRepository.save(pin);

        user.updatePinCnt(user.getPinCnt() + 1);
        userRepository.save(user);

        return CreatePinResponse.builder()
                .pinId(savedPin.getPinId())
                .build();
    }

    @Transactional
    public void deletePin(User user, List<Long> pinIds) {
        for (Long pinId : pinIds) {
            Pin pin = pinRepository.findByUserAndPinId(user, pinId)
                    .orElseThrow(() -> new GeneralException(Code.PIN_NOT_FOUND));

            pinRepository.delete(pin);

            user.updatePinCnt(user.getPinCnt() - 1);
            userRepository.save(user);

        }

    }

    @Transactional
    public void updatePin(User user, List<Long> pinIds, UpdatePinRequest request) {
        // 변경할 카테고리 조회 (외래키로 엮어 있는 컬럼을 수정하려면 엔티티를 조회해야한다)
        MyCategory newMyCategory = myCategoryRepository.findById(request.getMyCategoryId())
                .orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));

        for (Long pinId : pinIds) {
            Pin pin = pinRepository.findByUserAndPinId(user, pinId)
                    .orElseThrow(() -> new GeneralException(Code.PIN_NOT_FOUND));

            pin.updateMyCategory(newMyCategory);
        }
    }

}
