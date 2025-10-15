package com.umc.gusto.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Town {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(8)")
    private String townCode;

    @Column(nullable = false, columnDefinition = "VARCHAR(15)")
    private String townName;
}