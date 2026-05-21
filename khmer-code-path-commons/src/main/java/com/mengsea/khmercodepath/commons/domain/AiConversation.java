package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "ai_conversations")
@Getter
@Setter
@NoArgsConstructor
public class AiConversation {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "uuid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private AiSectionType sectionType = AiSectionType.GENERAL;

    @Column(name = "section_ref")
    private String sectionRef;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean deleted;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
