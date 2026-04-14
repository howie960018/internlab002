package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseBean;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseBeanRepository extends JpaRepository<CourseBean, Long> {
    boolean existsByCourseName(String courseName);
    boolean existsByCourseNameAndIdNot(String courseName, Long id);
}
