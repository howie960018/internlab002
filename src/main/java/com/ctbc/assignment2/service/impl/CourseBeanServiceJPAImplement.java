package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.service.CourseBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseBeanServiceJPAImplement implements CourseBeanService {

    @Autowired
    private CourseBeanRepository repo;

    @Override
    public List<CourseBean> findAll() {
        return repo.findAll();
    }

    @Override
    public CourseBean findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    @Override
    public CourseBean save(CourseBean course) {
        if (course.getId() != null) {
            // 更新：排除自身檢查是否重複
            if (repo.existsByCourseNameAndIdNot(course.getCourseName(), course.getId())) {
                throw new DuplicateCourseNameException("課程名稱已存在：" + course.getCourseName());
            }
        } else {
            // 新增：檢查是否已存在同名
            if (repo.existsByCourseName(course.getCourseName())) {
                throw new DuplicateCourseNameException("課程名稱已存在：" + course.getCourseName());
            }
        }
        return repo.save(course);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
