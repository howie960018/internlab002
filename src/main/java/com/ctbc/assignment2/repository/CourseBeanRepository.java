package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseBean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface CourseBeanRepository
        extends JpaRepository<CourseBean, UUID> {

    // 新增時使用
    boolean existsByCourseName(String courseName);

    // 更新時使用（排除自己）
    boolean existsByCourseNameAndIdNot(String courseName, UUID id);

    // 依分類查詢課程
    List<CourseBean> findByCategoryId(UUID categoryId);
}
