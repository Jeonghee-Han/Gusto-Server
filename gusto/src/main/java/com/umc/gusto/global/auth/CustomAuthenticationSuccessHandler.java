package com.umc.gusto.global.auth;

import com.umc.gusto.domain.user.entity.Social;
import com.umc.gusto.global.auth.model.CustomOAuth2User;
import com.umc.gusto.global.auth.model.OAuthAttributes;
import com.umc.gusto.global.auth.model.Tokens;
import com.umc.gusto.global.auth.service.JwtService;
import com.umc.gusto.global.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final AuthService authService;
    @Value("${default.login.redirect.url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if(authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            Social socialInfo = oAuth2User.getSocialInfo();

            response.setCharacterEncoding("utf-8");

            if(socialInfo != null){
                String userUUID = String.valueOf(socialInfo.getUser().getUserId());

                Tokens tokens = jwtService.createAndSaveTokens(userUUID);

                String uri = UriComponentsBuilder.fromUriString(redirectUrl + "/member")
                        .queryParam("X-Auth-Token", tokens.getAccessToken())
                        .queryParam("refresh-Token", tokens.getRefreshToken())
                        .toUriString();

                response.sendRedirect(uri);
                return;
            }

            OAuthAttributes attributes = oAuth2User.getOAuthAttributes();
            String uri = UriComponentsBuilder.fromUriString(redirectUrl + "/new-user")
                    .queryParam("nickname", attributes.getNickname())
                    .queryParam("profileImg", attributes.getProfileImg())
                    .queryParam("gender", attributes.getGender().name())
                    .queryParam("age", attributes.getAge().name())
                    .toUriString();

            response.sendRedirect(uri);
        }
    }

}
