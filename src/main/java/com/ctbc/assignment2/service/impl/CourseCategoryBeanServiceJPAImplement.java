package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseCategoryBeanServiceJPAImplement implements CourseCategoryBeanService {

    @Autowired
    private CourseCategoryBeanRepository repo;

    @Override
    public List<CourseCategoryBean> findAll() {
        return repo.findAll();
    }

    @Override
    public CourseCategoryBean findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    public CourseCategoryBean save(CourseCategoryBean category) {
        if (category.getId() != null) {
            // 更新：載入既有實體保留 courses 集合，排除自身檢查重複名稱
            CourseCategoryBean existing = repo.findById(category.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + category.getId()));
            if (repo.existsByCategoryNameAndIdNot(category.getCategoryName(), category.getId())) {
                throw new DuplicateCourseNameException("類別名稱已存在：" + category.getCategoryName());
            }
            existing.setCategoryName(category.getCategoryName());
            return repo.save(existing);
        }
        // 新增：檢查是否已存在同名
        if (repo.existsByCategoryName(category.getCategoryName())) {
            throw new DuplicateCourseNameException("類別名稱已存在：" + category.getCategoryName());
        }
        return repo.save(category);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
