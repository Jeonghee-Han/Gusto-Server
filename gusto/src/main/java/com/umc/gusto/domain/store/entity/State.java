package com.umc.gusto.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class State {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(2)")
    private String stateCode;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private String stateName;
}