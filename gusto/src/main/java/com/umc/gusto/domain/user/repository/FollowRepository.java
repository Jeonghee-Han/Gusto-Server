package com.umc.gusto.domain.user.repository;

import com.umc.gusto.domain.user.entity.Follow;
import com.umc.gusto.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    @Query("SELECT f FROM Follow f WHERE f.follower = :follower AND f.followId < :followId ORDER BY f.createdAt DESC, f.followId DESC")
    Page<Follow> findFollowList(@Param("follower") User follower, @Param("followId") Long followId, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.follower = :follower ORDER BY f.createdAt DESC, f.followId DESC")
    Page<Follow> findFollowList(@Param("follower") User follower, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.following = :following AND f.followId < :followId ORDER BY f.createdAt DESC, f.followId DESC")
    Page<Follow> findFollwerList(@Param("following") User following, @Param("followId") Long followId, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.following = :following ORDER BY f.createdAt DESC, f.followId DESC")
    Page<Follow> findFollwerList(@Param("following") User following, Pageable pageable);
}