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
        name = "quiz_submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_quiz_submission", columnNames = {"quiz_id", "student_user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", referencedColumnName = "uuid", nullable = false)
    private User student;

    /** SUBMITTED | FAILED */
    @Column(nullable = false)
    private String status = "SUBMITTED";

    @Column
    private Integer score;

    @Column(name = "fail_reason")
    private String failReason;

    /** JSON map of {questionId: selectedOptionIndex} */
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    @CreationTimestamp
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
