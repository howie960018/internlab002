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
}
