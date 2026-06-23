package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.SchoolStatus;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schools")
@Getter
@Setter
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 128)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SchoolStatus status = SchoolStatus.ACTIVE;

    @Column(name = "registration_open", nullable = false)
    private boolean registrationOpen = true;

    @Column(name = "public_courses_enabled", nullable = false)
    private boolean publicCoursesEnabled = false;

    @Column(name = "cover_storage_key", length = 512)
    private String coverStorageKey;

    @Column(length = 512)
    private String tagline;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
