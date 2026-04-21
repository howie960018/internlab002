package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


@Service
public class CourseBeanServiceJPAImplement implements CourseBeanService {

    @Autowired
    private CourseBeanRepository repo;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @Override
    public List<CourseBean> findAll() {
        return repo.findAll();
    }

    @Override
    public CourseBean findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found: " + id));
    }

    @Override
    public CourseBean save(CourseBean course) {

        if (course.getId() != null) {
            // 情況 A：更新
            if (repo.existsByCourseNameAndIdNot(
                    course.getCourseName(), course.getId())) {
                throw new DuplicateCourseNameException(
                        "課程名稱已存在：" + course.getCourseName());
            }
        } else {
            // 情況 B：新增
            if (repo.existsByCourseName(course.getCourseName())) {
                throw new DuplicateCourseNameException(
                        "課程名稱已存在：" + course.getCourseName());
            }
        }

        return repo.save(course);
    }

    @Override
    public void deleteById(UUID id) {
        CourseBean course = findById(id);
        repo.delete(course);
    }

    @Override
    public List<CourseBean> findByCategoryId(UUID categoryId) {
        categoryService.findById(categoryId); // 分類不存在直接擋掉
        return repo.findByCategoryId(categoryId);
    }
}
