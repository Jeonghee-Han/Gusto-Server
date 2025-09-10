package com.umc.gusto.domain.myCategory.controller;

import com.umc.gusto.domain.myCategory.model.request.CreateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.request.UpdateMyCategoryRequest;
import com.umc.gusto.domain.myCategory.model.response.MyCategoryResponse;
import com.umc.gusto.domain.myCategory.model.response.PagingResponse;
import com.umc.gusto.domain.myCategory.model.response.PinByMyCategoryResponse;
import com.umc.gusto.domain.myCategory.service.MyCategoryService;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.auth.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/myCategories")
public class MyCategoryController {
    private final MyCategoryService myCategoryService;

    /**
     * 카테고리 전체 조회
     * [GET] /myCategories?nickname={nickname}&townCode={townCode}&myCategoryId={myCategoryId}
     */
    @GetMapping
    public ResponseEntity<PagingResponse> allMyCategory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "nickname", required = false) String nickname,
            @RequestParam(name = "townCode", required = false) String townCode,
            @RequestParam(name = "myCategoryId", required = false) Long myCategoryId) {     // paging 처리를 위해 마지막 리턴 myCategoryId 사용

        User user = (nickname == null) ? authUser.getUser() : null;
        PagingResponse pagingResponse = myCategoryService.getAllMyCategory(user, nickname, townCode, myCategoryId);

        return ResponseEntity.status(HttpStatus.OK).body(pagingResponse);
    }

    /**
     * 카테고리 별 가게 목록 조회
     * [GET] /myCategories/pins?nickname={nickname}&myCategoryId={myCategoryId}&townCode={townCode}&pinId={pinId}&storeName={storeName}&sort={sort}
     */
    @GetMapping("/pins")             // 나의 찜을 조회 할 시 nickname 값을 받지 않고, nickname이 조회될 경우 townCode 받지 않음
    public ResponseEntity<PagingResponse> allPinByMyCategory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "nickname", required = false) String nickname,
            @RequestParam(name = "myCategoryId") Long myCategoryId,
            @RequestParam(name = "townCode", required = false) String townCode,
            @RequestParam(name = "pinId", required = false) Long pinId,
            @RequestParam(name = "storeName", required = false) String storeName,
            @RequestParam(name = "sort", required = false) String sort  // paging 처리를 위해 마지막 리턴 pinId 사용
            ) {

        User user = (nickname == null) ? authUser.getUser() : null;
        PagingResponse pagingResponse = myCategoryService.getAllPinByMyCategory(user, nickname, myCategoryId, townCode, pinId, storeName, sort);

        return ResponseEntity.status(HttpStatus.OK).body(pagingResponse);
    }

    /**
     * 내 카테고리 생성
     * [POST] /myCategories
     */
    @PostMapping
    public ResponseEntity<?> createMyCategory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreateMyCategoryRequest request
    ) {
            User user = authUser.getUser();
            myCategoryService.createMyCategory(user, request);

            return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 내 카테고리 수정
     * [PATCH] /myCategories/{myCategoryId}
     */
    @PatchMapping("/{myCategoryId}")
    public ResponseEntity<?> modifyMyCategory(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long myCategoryId,
            @RequestBody UpdateMyCategoryRequest request
    ) {
        User user = authUser.getUser();
        myCategoryService.modifyMyCategory(user, myCategoryId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 내 카테고리 삭제
     * [DELETE] /myCategories?myCategoryId={myCategoryId}&myCategoryId={myCategoryId}...
     */
    @DeleteMapping
    public ResponseEntity<?> deleteMyCategory(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "myCategoryId") List<Long> myCategoryIds) {
            User user = authUser.getUser();
            myCategoryService.deleteMyCategories(user, myCategoryIds);

            return ResponseEntity.status(HttpStatus.OK).build();

    }

    /**
     * 내 카테고리 내 가게 이동
     * [PATCH] /myCateo
     */
}