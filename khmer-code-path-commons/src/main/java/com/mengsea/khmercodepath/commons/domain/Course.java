package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.CourseLevel;
import com.mengsea.khmercodepath.commons.domain.converter.CourseTechnologiesConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 255)
    private String institution;

    @Column(name = "institution_logo", length = 64)
    private String institutionLogo;

    @Column(name = "institution_color", nullable = false, length = 16)
    private String institutionColor = "#8b5cf6";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CourseLevel level = CourseLevel.BEGINNER;

    @Column(nullable = false)
    private int pts = 150;

    @Column(name = "bg_color", nullable = false, length = 128)
    private String bgColor = "from-slate-900 to-slate-700";

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Convert(converter = CourseTechnologiesConverter.class)
    @Column(name = "technologies_json", nullable = false, columnDefinition = "TEXT")
    private List<CourseTechnology> technologies = new ArrayList<>();

    @Column(length = 500)
    private String prerequisite;

    @Column(length = 255)
    private String achievement;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private boolean published = true;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "uuid")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
