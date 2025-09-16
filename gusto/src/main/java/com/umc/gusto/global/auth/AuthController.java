package com.umc.gusto.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.gusto.domain.user.model.response.FirstLogInResponse;
import com.umc.gusto.global.auth.model.Tokens;
import com.umc.gusto.global.auth.service.AuthService;
import com.umc.gusto.global.auth.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final AuthService authService;

    @GetMapping("/result/member")
    public void returnLoginResult(HttpServletResponse response,
                                  @RequestParam(name = "X-Auth-Token") String XAuthToken,
                                  @RequestParam(name = "refresh-Token") String refreshToken) {
        response.setHeader("X-Auth-Token", XAuthToken);
        response.setHeader("refresh-Token", refreshToken);
    }

    @GetMapping("/result/new-user")
    public void returnLoginResult(HttpServletResponse response,
                                  @RequestParam(name = "temp-token") String tempToken,
                                  @RequestParam(value = "nickname") String nickname,
                                  @RequestParam(value = "profileImg") String profileImg,
                                  @RequestParam(value = "gender", required = false) String gender,
                                  @RequestParam(value = "age", required = false) String age) throws IOException {
        FirstLogInResponse firstLogInResponse = FirstLogInResponse.builder()
                .nickname(nickname)
                .profileImg(profileImg)
                .gender(gender)
                .age(age)
                .build();

        response.setHeader("temp-token", tempToken);
        response.getWriter().write(objectMapper.writeValueAsString(firstLogInResponse));
    }

    @PostMapping("/reissue-token")
    public ResponseEntity reissueToken(@RequestHeader("X-AUTH-TOKEN") String accessToken,
                                       @RequestHeader("refresh-Token") String refreshToken) {
        Tokens tokens = jwtService.reissueToken(accessToken, refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-AUTH-TOKEN", tokens.getAccessToken());
        headers.add("refresh-token", tokens.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .build();
    }

    @GetMapping("/decoding")
    public ResponseEntity decoding(@RequestParam("value") String value) {
        String result = authService.decode(value);

        return ResponseEntity.ok()
                .body(result);
    }

    /*
    *
    * auth 로그인이 불가능한 백엔드에서
    * 테스트 시 필요한 토큰 발급을 위한 API
    *
    * */
    @PostMapping("/token")
    public ResponseEntity getTestToken(@RequestHeader("back-token") String backToken, @RequestParam("nickname") String nickname) {
        String result = authService.getTestToken(backToken, nickname);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-AUTH-TOKEN", result);

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }
}
