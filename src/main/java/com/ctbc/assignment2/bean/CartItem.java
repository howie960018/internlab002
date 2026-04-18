package com.ctbc.assignment2.bean;

import java.io.Serializable;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long courseId;
    private String courseName;
    private Double price;

    public CartItem() {
    }

    public CartItem(Long courseId, String courseName, Double price) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.price = price;
    }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
