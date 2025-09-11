package com.umc.gusto.global.auth.service;


import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocialService {
    private final AuthService authService;
    private final RestClient restClient = RestClient.create();
    private static final String AUTH_TYPE = "Bearer ";
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    public void checkUserInfo(String provider, String encodedId, String encodedToken) {
        // TODO: ACCESS TOKEN 복호화
        String providerId = authService.decode(encodedId);
        String accessToken = authService.decode(encodedToken);

        String header = AUTH_TYPE + accessToken;
        String id = "";

        if(provider.equals("NAVER")) {
            Map result = restClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", header)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_NOT_FOUND_TOKEN);
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_FIND_ERROR);
                    }))
                    .body(Map.class);

            Map<String, String> response = new HashMap<>((LinkedHashMap) result.get("response"));
            id = response.get("id");
        } else if(provider.equals("GOOGLE")) {
            log.info("Google start");
            Map result = restClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", header)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_NOT_FOUND_TOKEN);
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_FIND_ERROR);
                    }))
                    .body(Map.class);

            id = String.valueOf(result.get("id"));
        } else {
            Map result = restClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", header)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_NOT_FOUND_TOKEN);
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_FIND_ERROR);
                    }))
                    .body(Map.class);

            id = String.valueOf(result.get("id"));
        }

        if(!providerId.equals(id)) {
            throw new GeneralException(Code.UNMATCHED_AUTH_INFO);
        }
    }


    public void disconnectAccount(String provider, String accessToken) {
        // TODO: ACCESS TOKEN 복호화

        if(provider.equals("NAVER")) {
            StringBuilder uri = new StringBuilder("https://nid.naver.com/oauth2.0/token?grant_type=delete&");
            uri.append("client_id=").append(NAVER_CLIENT_ID).append("&")
                    .append("client_secret=").append(NAVER_CLIENT_SECRET).append("&")
                    .append("access_token=").append(accessToken);
            System.out.println(uri);
            Map result = restClient.post()
                    .uri(uri.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_NOT_FOUND_TOKEN);
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_FIND_ERROR);
                    }))
                    .body(Map.class);

            String resultCode = String.valueOf(result.get("result"));

            log.info(resultCode);
        } else if(provider.equals("GOOGLE")) {
            StringBuilder uri = new StringBuilder("https://oauth2.googleapis.com/revoke?");
            uri.append("access_token=").append(accessToken);

            Map result = restClient.get()
                    .uri(uri.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_NOT_FOUND_TOKEN);
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_FIND_ERROR);
                    }))
                    .body(Map.class);

            log.info(result.toString());
        } else {
            String header = AUTH_TYPE + accessToken;

            String result = restClient.get()
                    .uri("https://kapi.kakao.com/v1/user/unlink")
                    .header("Authorization", header)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_NOT_FOUND_TOKEN);
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new GeneralException(Code.OAUTH_FIND_ERROR);
                    }))
                    .body(String.class);
        }
    }
}
