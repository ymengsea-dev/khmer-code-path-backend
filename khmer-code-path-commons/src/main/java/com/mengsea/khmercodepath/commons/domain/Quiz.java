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

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private LmsClass lmsClass;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;

    @Column(name = "question_count", nullable = false)
    private int questionCount = 0;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    /** ASSIGNMENT = open practice with deadline; EXAM = proctored (tab-switch fails attempt). */
    @Column(nullable = false)
    private String kind = "ASSIGNMENT";

    @Column(nullable = false)
    private String status = "PUBLISHED";

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
