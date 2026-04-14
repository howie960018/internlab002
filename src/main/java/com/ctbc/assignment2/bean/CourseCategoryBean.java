package com.ctbc.assignment2.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "course_category")
public class CourseCategoryBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "類別名稱不可為空")
    private String categoryName;

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<CourseBean> courses;

    @PreRemove
    private void preRemove() {
        if (courses != null) {
            courses.forEach(course -> course.setCategory(null));
        }
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<CourseBean> getCourses() { return courses; }
    public void setCourses(List<CourseBean> courses) { this.courses = courses; }
}
