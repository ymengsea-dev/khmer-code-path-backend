package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lms_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LmsClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "uuid", nullable = false)
    private User teacher;

    @Column(length = 128)
    private String semester;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(length = 255)
    private String schedule;

    @Column(name = "room_number", length = 128)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ClassStatus status = ClassStatus.ACTIVE;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "weight_attendance", nullable = false)
    private int weightAttendance = 10;

    @Column(name = "weight_assignment", nullable = false)
    private int weightAssignment = 10;

    @Column(name = "weight_quiz", nullable = false)
    private int weightQuiz = 5;

    @Column(name = "weight_midterm", nullable = false)
    private int weightMidterm = 25;

    @Column(name = "weight_final_exam", nullable = false)
    private int weightFinalExam = 50;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
