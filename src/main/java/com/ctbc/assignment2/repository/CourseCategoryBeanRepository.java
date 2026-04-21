package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface CourseCategoryBeanRepository
        extends JpaRepository<CourseCategoryBean, UUID> {

    // 新增時使用
    boolean existsByCategoryName(String categoryName);

    // 更新時使用（排除自己）
    boolean existsByCategoryNameAndIdNot(String categoryName, UUID id);
}
