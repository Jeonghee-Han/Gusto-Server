package com.umc.gusto.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class City {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(5)")
    private String cityCode;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private String cityName;
}