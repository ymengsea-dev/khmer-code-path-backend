package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.InfrastructureVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "infrastructure_status")
@Getter
@Setter
public class InfrastructureStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(name = "status_text", nullable = false, length = 128)
    private String statusText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InfrastructureVariant variant = InfrastructureVariant.SUCCESS;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean deleted = false;
}
