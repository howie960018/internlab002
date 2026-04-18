package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 課程類別 Repository
 * 繼承 JpaRepository 來取得操作 CourseCategoryBean 資料表的 CRUD 能力。
 */
public interface CourseCategoryBeanRepository extends JpaRepository<CourseCategoryBean, Long> {
    
    // 自動根據方法名稱生成查詢：檢查該類別名稱是否存在
    boolean existsByCategoryName(String categoryName);

    // 依名稱取得類別
    java.util.Optional<CourseCategoryBean> findByCategoryName(String categoryName);
    
    // 檢查指定的名稱是否存在，且排除目前正在更新的 id (避免自己跟自己比名稱重複)
    boolean existsByCategoryNameAndIdNot(String categoryName, Long id);

    // 只抓主類別（parent 為 null）
    java.util.List<CourseCategoryBean> findByParentIsNullOrderByCategoryName();

    // 取得指定父類別底下的子類別
    java.util.List<CourseCategoryBean> findByParentIdOrderByCategoryName(Long parentId);

    // 檢查是否還有子類別
    boolean existsByParentId(Long parentId);
}
