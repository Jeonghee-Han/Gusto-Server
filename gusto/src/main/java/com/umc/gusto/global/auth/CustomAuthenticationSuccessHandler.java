package com.umc.gusto.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.gusto.domain.user.entity.Social;
import com.umc.gusto.global.auth.model.CustomOAuth2User;
import com.umc.gusto.global.auth.model.Tokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final OAuthService oAuthService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if(authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            Social socialInfo = oAuth2User.getSocialInfo();

            response.setCharacterEncoding("utf-8");

            if(socialInfo.getSocialStatus() == Social.SocialStatus.CONNECTED){
                String userUUID = String.valueOf(socialInfo.getUser().getUserid());

                Tokens tokens = jwtService.createAndSaveTokens(userUUID);
                
                response.setHeader("X-AUTH-TOKEN", tokens.getAccessToken());
                response.setHeader("refresh-token", tokens.getRefreshToken());
                response.getWriter();
                return;
            }

            // social temporal token 및 OAuthAttributes return
            response.setContentType("application/json");
            response.setHeader("Location", "http://{domain}/sign-in");
            response.setHeader("temp-token", String.valueOf(socialInfo.getTemporalToken()));

            // TODO: 차후 응답 코드 형태 맞춰 리팩토링할 것
            String body = objectMapper.writeValueAsString(
                    oAuthService.generateFirstLogInRes(oAuth2User.getOAuthAttributes())
            );

            response.getWriter().write(body);
        }
    }
}