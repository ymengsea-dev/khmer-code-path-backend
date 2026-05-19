package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.DepartmentAccent;
import com.mengsea.khmercodepath.commons.constant.DepartmentStatus;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Getter
@Setter
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String faculty;

    @Column(name = "head_of_dept", length = 255)
    private String headOfDept;

    @Column(name = "faculty_count", nullable = false)
    private int facultyCount = 0;

    @Column(name = "capacity_percent", nullable = false)
    private int capacityPercent = 50;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DepartmentAccent accent = DepartmentAccent.VIOLET;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hod_user_id", referencedColumnName = "uuid")
    private User hodUser;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
