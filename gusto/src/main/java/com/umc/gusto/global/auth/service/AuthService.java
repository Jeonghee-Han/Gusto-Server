package com.umc.gusto.global.auth.service;

import com.umc.gusto.domain.user.entity.Social;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.domain.user.repository.SocialRepository;
import com.umc.gusto.domain.user.repository.UserRepository;
import com.umc.gusto.global.auth.model.CustomOAuth2User;
import com.umc.gusto.global.auth.model.OAuthAttributes;
import com.umc.gusto.global.auth.model.Tokens;
import com.umc.gusto.global.config.secret.JwtConfig;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import com.umc.gusto.global.util.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final SocialRepository socialRepository;
    private final JwtService jwtService;
    private final RedisService redisService;
    @Value("${default.img.url}")
    private String DEFAULT_PROFILE_IMG;
    @Value("${gusto.security.private-key}")
    private String ENCODING_PRIVATE_KEY;
    @Value("${gusto.security.initialize-vector}")
    private String INITIALIZE_VECTOR;
    @Value("${gusto.security.encoding-type}")
    private String ENCODING_TYPE;
    @Value("${gusto.security.back-token}")
    private String BACK_TOKEN;


    // 유저 불러오기 - 해당 유저의 security context가 저장됨
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("***********************");
        log.info(userRequest.getAccessToken().getTokenValue());
        log.info("***********************");

        // provider - string to enum으로 변환
        Social.SocialType provider = Social.SocialType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        String userNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(provider, userNameAttribute, oAuth2User.getAttributes());
        log.info(oAuthAttributes.getId());
        log.info("***********************");

        Optional<Social> socialInfo = socialRepository.findBySocialTypeAndProviderId(provider, oAuthAttributes.getId());

        Social info;

        if(socialInfo.isEmpty()) {
             info = null;
        } else {
            info = socialInfo.get();
        }

        return CustomOAuth2User.builder()
                .delegate(oAuth2User)
                .oAuthAttributes(oAuthAttributes)
                .socialInfo(info)
                .build();
    }

    public String decode(String cryptogram) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCODING_PRIVATE_KEY.getBytes("UTF-8"), "AES");
            IvParameterSpec IV = new IvParameterSpec(INITIALIZE_VECTOR.substring(0, 16).getBytes());

            Cipher c = Cipher.getInstance(ENCODING_TYPE);

            c.init(Cipher.DECRYPT_MODE, secretKey, IV);

            byte[] decodeByte = Base64.getDecoder().decode(cryptogram.getBytes());

            return new String(c.doFinal(decodeByte), "UTF-8");
        } catch (InvalidAlgorithmParameterException | UnsupportedEncodingException |
                NoSuchPaddingException | IllegalBlockSizeException |
                NoSuchAlgorithmException | InvalidKeyException |
                BadPaddingException e) {
            throw new GeneralException(Code.INTERNAL_SEVER_ERROR);
        }
    }

    public String getTestToken(String backToken, String nickname) {
        if(!backToken.equals(BACK_TOKEN)) {
            throw new GeneralException(Code.INVALID_BACK_TOKEN);
        }

        User user = userRepository.findByNickname(nickname).orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));

        // access-token 및 refresh-token 생성
        Tokens tokens = jwtService.createToken(String.valueOf(user.getUserId()));
        redisService.setValuesWithTimeout(tokens.getRefreshToken(), String.valueOf(user.getUserId()), JwtConfig.REFRESH_TOKEN_VALID_TIME);

        return tokens.getAccessToken();
    }
}
