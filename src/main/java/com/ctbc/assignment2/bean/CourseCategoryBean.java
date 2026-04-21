

/**
 * 課程類別資料模型 (Entity)
 * 代表資料庫中的課程類別表格，主要用來分類不同的課程。
 */
package com.ctbc.assignment2.bean;

import java.util.List;
import java.util.UUID;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "course_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseCategoryBean {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "類別名稱不可以為空")
    private String categoryName;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<CourseBean> courses;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }
}