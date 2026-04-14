package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import java.util.List;

public interface CourseCategoryBeanService {
    List<CourseCategoryBean> findAll();
    CourseCategoryBean findById(Long id);
    CourseCategoryBean save(CourseCategoryBean category);
    void deleteById(Long id);
}
