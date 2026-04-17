package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseBean;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA Repository
 * 這裡繼承 JpaRepository 介面，不需要自己寫 SQL，Spring 會自動實作大部分的資料庫操作 (CRUD：增刪改查)。
 * <CourseBean, Long> 分別代表要操作的實體類別與主鍵的資料型別。
 */
public interface CourseBeanRepository extends JpaRepository<CourseBean, Long> {
    
    // Spring Data JPA 的命名規範：
    // 方法名稱若以 "existsBy" 開頭加上欄位名稱，Spring 就會自動轉換成檢查是否存在該條件的 SQL 語句。
    boolean existsByCourseName(String courseName);

    // 尋找「除了指定 Id 外」是否還有相同課程名稱的資料，常用於更新驗證時。
    boolean existsByCourseNameAndIdNot(String courseName, Long id);

    // 批次新增前，檢查這批名稱是否已存在於資料庫
    boolean existsByCourseNameIn(java.util.List<String> courseNames);

    // 檢查指定類別是否仍有課程
    boolean existsByCategoryId(Long categoryId);

    // 取得指定類別下的課程
    java.util.List<CourseBean> findByCategoryId(Long categoryId);

    // 依類別集合分頁查詢課程
    org.springframework.data.domain.Page<CourseBean> findByCategoryIdIn(java.util.List<Long> categoryIds,
                                                                         org.springframework.data.domain.Pageable pageable);

        // 依名稱關鍵字分頁查詢課程
        org.springframework.data.domain.Page<CourseBean> findByCourseNameContainingIgnoreCase(String courseName,
                                                   org.springframework.data.domain.Pageable pageable);

        // 依類別集合與名稱關鍵字分頁查詢課程
        org.springframework.data.domain.Page<CourseBean> findByCategoryIdInAndCourseNameContainingIgnoreCase(
            java.util.List<Long> categoryIds,
            String courseName,
            org.springframework.data.domain.Pageable pageable);
}
