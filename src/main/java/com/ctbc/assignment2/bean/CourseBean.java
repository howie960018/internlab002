package com.ctbc.assignment2.bean;

import java.util.Date;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "course")
public class CourseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "課程名稱不可為空")
    private String courseName;

    @NotNull(message = "價格不可為空")
    @PositiveOrZero(message = "價格不可為負數")
    private Double price;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private CourseCategoryBean category;

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

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public CourseCategoryBean getCategory() { return category; }
    public void setCategory(CourseCategoryBean category) { this.category = category; }

    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
}
