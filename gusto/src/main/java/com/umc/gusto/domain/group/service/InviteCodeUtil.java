package com.umc.gusto.domain.group.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;

@Component
public class InviteCodeUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String SECRET;

    private static final long EXPIRATION_HOURS = 168;
    private static final int RANDOM_STRING_LENGTH = 10;

    // 생성자 주입
    public InviteCodeUtil(@Value("${invite.code.secret-key}") String secret) {
        this.SECRET = secret;
    }

    // 내부에서 사용할 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class InviteCodePayload {
        private Long groupId;
        private Long exp;
        private Long iat;
        private String rnd;
    }

    // 초대 코드 생성 (DTO와 ObjectMapper 사용)
    public String generateInviteCode(Long groupId) {
        try {
            long iat = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            long exp = LocalDateTime.now().plusHours(EXPIRATION_HOURS).toEpochSecond(ZoneOffset.UTC);
            // [테스트용 코드] 현재 시간에서 1초 뺌 (생성하자마자 만료됨)
            //long exp = LocalDateTime.now().minusSeconds(1).toEpochSecond(ZoneOffset.UTC);
            String rnd = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH);

            InviteCodePayload payloadObject = new InviteCodePayload(groupId, exp, iat, rnd);
            String payload = objectMapper.writeValueAsString(payloadObject);

            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            String signature = hmacSha256(encodedPayload, SECRET);
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_SEVER_ERROR);
        }
    }

    // 초대 코드 검증 및 groupId 추출
    public Long validateAndGetGroupId(String inviteCode) {
        try {
            String[] parts = inviteCode.split("\\.");
            if (parts.length != 2) {
                throw new GeneralException(Code.INVALID_INVITE_CODE_FORMAT);
            }

            String encodedPayload = parts[0];
            String signature = parts[1];

            String expectedSig = hmacSha256(encodedPayload, SECRET);
            if (!Objects.equals(expectedSig, signature)) {
                throw new GeneralException(Code.INVITE_CODE_SIGNATURE_MISMATCH);
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            InviteCodePayload payload = objectMapper.readValue(payloadJson, InviteCodePayload.class);

            if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) >= payload.getExp()) {
                throw new GeneralException(Code.INVITE_CODE_EXPIRED);
            }

            return payload.getGroupId();
        } catch (GeneralException e) {
            throw e; // 이미 처리된 예외는 그대로 다시 던짐
        } catch (Exception e) {
            // JSON 파싱 오류 등 기타 모든 예외
            throw new GeneralException(Code.INVALID_INVITE_CODE);
        }
    }

    // HMAC SHA256 서명
    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}