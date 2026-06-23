package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.FacultyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "faculties")
@Getter
@Setter
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", referencedColumnName = "id", nullable = false)
    private School school;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 512)
    private String tagline;

    @Column(name = "cover_storage_key", length = 512)
    private String coverStorageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FacultyStatus status = FacultyStatus.ACTIVE;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
