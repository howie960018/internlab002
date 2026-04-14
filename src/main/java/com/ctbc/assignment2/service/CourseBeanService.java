package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseBean;
import java.util.List;

public interface CourseBeanService {
    List<CourseBean> findAll();
    CourseBean findById(Long id);
    CourseBean save(CourseBean course);
    void deleteById(Long id);
}
