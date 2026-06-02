package com.mengsea.khmercodepath.commons.domain;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "material_library_items")
@Getter
@Setter
@NoArgsConstructor
public class MaterialLibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "uuid", nullable = false)
    private User teacher;

    @Column(nullable = false)
    private String title;

    @Column(name = "module_tag")
    private String moduleTag;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon_type", nullable = false)
    private LibraryIconType iconType = LibraryIconType.SLIDES;

    @Column(nullable = false)
    private String gradient = "from-violet-800 to-violet-600";

    /** Teacher-wide file storage (not a lesson template). */
    @Column(name = "file_pool", nullable = false)
    private boolean filePool = false;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
