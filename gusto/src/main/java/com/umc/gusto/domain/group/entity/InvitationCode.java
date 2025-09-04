package com.umc.gusto.domain.group.entity;

import com.umc.gusto.global.common.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvitationCode extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long invitationCodeId;

    @OneToOne
    @JoinColumn(name = "groupId", nullable = false)
    private Group group;

    @Column(columnDefinition = "VARCHAR(255)")
    private String code;
}
