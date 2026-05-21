package com.mengsea.khmercodepath.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notes")
@Getter
@Setter
@NoArgsConstructor
public class UserNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "uuid", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "body_html", nullable = false, columnDefinition = "TEXT")
    private String bodyHtml = "";

    @Column(nullable = false, length = 500)
    private String preview = "";

    @Column(name = "source_label")
    private String sourceLabel;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "material_id")
    private Long materialId;

    /** Comma-separated tags, e.g. Exam Prep,AI-Generated */
    @Column(nullable = false, length = 500)
    private String tags = "";

    @Column(nullable = false)
    private boolean deleted;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
