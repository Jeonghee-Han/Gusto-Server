package com.umc.gusto.domain.user.repository;

import com.umc.gusto.domain.user.entity.Social;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialRepository extends JpaRepository<Social, Long> {
    Optional<Social> findBySocialTypeAndProviderId(Social.SocialType socialType, String providerId);

    Integer countSocialsByUser(User user);

    Boolean existsByUserAndSocialType(User user, Social.SocialType socialType);

    Boolean existsBySocialTypeAndProviderId(Social.SocialType socialType, String providerId);

    List<Social> findByUser(User user);
}
