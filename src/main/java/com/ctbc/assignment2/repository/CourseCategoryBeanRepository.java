package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 課程分類的資料存取庫 (Repository)。
 * 繼承 JpaRepository 讓 Spring Data JPA 自動生成實作，提供新增、刪除、查詢等方法。
 * 泛型：<CourseCategoryBean, Long> 代表操作的實體是 CourseCategoryBean，主鍵是 Long。
 */
public interface CourseCategoryBeanRepository extends JpaRepository<CourseCategoryBean, Long> {
    
    /**
     * 自動產生 SQL 去資料庫檢查是否已經有這個分類名稱，避免新增的時候重複。
     * @param categoryName 欲檢查的分類名稱
     * @return true 代表已存在；false 代表不存在
     */
    boolean existsByCategoryName(String categoryName);

    /**
     * 檢查是否已經有這個分類名稱存在，但排除掉特定的一個 ID 不檢查。
     * 主要用於【編輯分類】的場景，以避免我們自己目前的名字被當作是重複的名字。
     * @param categoryName 想改成的分類名稱
     * @param id 要排除的分類 ID (自己原來的 ID)
     */
    boolean existsByCategoryNameAndIdNot(String categoryName, Long id);
}
