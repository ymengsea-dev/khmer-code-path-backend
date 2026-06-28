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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "assignment_submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assignment_submission",
                columnNames = {"assignment_id", "student_user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", referencedColumnName = "uuid", nullable = false)
    private User student;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String status = "SUBMITTED";

    @CreationTimestamp
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(length = 64)
    private String grade;

    /** 0–100 score for weighted grade calculation (100 = submitted / completion). */
    @Column(name = "score_percent", precision = 5, scale = 2)
    private java.math.BigDecimal scorePercent;
}
