package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;



import org.springframework.transaction.annotation.Transactional;


@Service
public class CourseCategoryBeanServiceJPAImplement
        implements CourseCategoryBeanService {

    @Autowired
    private CourseCategoryBeanRepository repo;

    @Override
    public List<CourseCategoryBean> findAll() {
        return repo.findAll();
    }

    @Override
    public CourseCategoryBean findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    public CourseCategoryBean save(CourseCategoryBean category) {

        // 情況 A：更新既有分類
        if (category.getId() != null) {
            CourseCategoryBean existing =
                    repo.findById(category.getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Category not found: " + category.getId()));

            if (repo.existsByCategoryNameAndIdNot(
                    category.getCategoryName(), category.getId())) {
                throw new DuplicateCourseNameException(
                        "類別名稱已存在：" + category.getCategoryName());
            }

            existing.setCategoryName(category.getCategoryName());
            return repo.save(existing);
        }

        // 情況 B：新增分類
        if (repo.existsByCategoryName(category.getCategoryName())) {
            throw new DuplicateCourseNameException(
                    "類別名稱已存在：" + category.getCategoryName());
        }

        return repo.save(category);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        CourseCategoryBean category = findById(id);

        if (category.getCourses() != null) {
            for (CourseBean course : category.getCourses()) {
                course.setCategory(null);
            }
        }

        repo.delete(category);
    }
}