package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseCategoryBeanRepository extends JpaRepository<CourseCategoryBean, Long> {
    boolean existsByCategoryName(String categoryName);
    boolean existsByCategoryNameAndIdNot(String categoryName, Long id);
}
