package com.ctbc.assignment2.bean;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "chapter")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseBean course;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(length = 512)
    private String videoPlaceholderUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseBean getCourse() {
        return course;
    }

    public void setCourse(CourseBean course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getVideoPlaceholderUrl() {
        return videoPlaceholderUrl;
    }

    public void setVideoPlaceholderUrl(String videoPlaceholderUrl) {
        this.videoPlaceholderUrl = videoPlaceholderUrl;
    }
}
