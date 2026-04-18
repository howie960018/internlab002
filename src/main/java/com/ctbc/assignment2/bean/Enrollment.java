package com.ctbc.assignment2.bean;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "enrollment",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseBean course;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date enrolledAt;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @PrePersist
    public void onCreate() {
        enrolledAt = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getStudent() { return student; }
    public void setStudent(AppUser student) { this.student = student; }

    public CourseBean getCourse() { return course; }
    public void setCourse(CourseBean course) { this.course = course; }

    public Date getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Date enrolledAt) { this.enrolledAt = enrolledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
