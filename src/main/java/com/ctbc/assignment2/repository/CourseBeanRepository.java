package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseBean;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * 課程資料存取庫 (Repository)。
 * 透過繼承 JpaRepository，Spring Data JPA 會自動幫我們實作 CRUD 等基本的資料庫操作。
 * 這邊指定的泛型：<CourseBean, UUID>，代表這個 Repository 是「為了操作 CourseBean，且其主鍵(ID)型態為 UUID」。
 */
public interface CourseBeanRepository extends JpaRepository<CourseBean, UUID> {
    
    /**
     * 檢查給定的「課程名稱」在資料庫中是否已經存在 (回傳 boolean)。
     * 這邊是 Spring Data JPA 特有的魔術查詢機制：透過定義好的命名規則 (existsBy...PropertyName)，自動幫我們編寫 SQL。
     * @param courseName 想檢查的課程名字
     */
    boolean existsByCourseName(String courseName);

    /**
     * 檢查給定的「課程名稱」在資料庫中是否已經存在，但是排除掉「某個特定的 ID」。
     * 這在【更新表單】時非常有用，可以確保你新改的名字不會跟別人的名字重複，但可以跟目前的自己一致。
     * @param courseName 想檢查的新課程名稱
     * @param id 排除的課程 ID (通常傳入欲更新課程的自己的 ID)
     */
    boolean existsByCourseNameAndIdNot(String courseName, UUID id);
}
