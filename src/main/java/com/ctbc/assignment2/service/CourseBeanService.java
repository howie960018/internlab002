package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 課程服務介面 (Service Interface)
 * 
 * - 介面與實作的關係 (Interface Inheritance/Implementation)：
 *   使用介面能將「規格(做什麼)」和「實作細節(如何做)」分離，遵守了物件導向設計原則。
 *   這使我們可以在不影響其他程式碼的狀況下，抽換不同的實作方式。
 */
public interface CourseBeanService {
    
    /**
     * 查詢所有課程
     */
    List<CourseBean> findAll();

    /**
     * 取得課程總筆數
     */
    long count();

    /**
     * 根據 ID 尋找單一課程
     */
    CourseBean findById(Long id);

    /**
     * 儲存課程（處理新增與修改）
     */
    CourseBean save(CourseBean course);

    /**
     * 根據 ID 刪除課程
     */
    void deleteById(Long id);

    /**
     * 分頁查詢課程
     */
    Page<CourseBean> findPage(Pageable pageable);

    /**
     * 批次刪除課程
     */
    void deleteBatch(List<Long> ids);

    /**
     * 批次新增課程（All-or-Nothing）
     */
    List<CourseBean> saveBatch(List<CourseBean> courses);

    /**
     * 查詢指定類別下的課程
     */
    List<CourseBean> findByCategoryId(Long categoryId);

    /**
     * 查詢指定類別集合下的課程（分頁）
     */
    Page<CourseBean> findPageByCategoryIds(List<Long> categoryIds, Pageable pageable);

    /**
     * 依名稱關鍵字分頁查詢課程
     */
    Page<CourseBean> findPageByName(String keyword, Pageable pageable);

    /**
     * 依類別集合與名稱關鍵字分頁查詢課程
     */
    Page<CourseBean> findPageByCategoryIdsAndName(List<Long> categoryIds, String keyword, Pageable pageable);

    /**
     * 依狀態分頁查詢課程
     */
    Page<CourseBean> findPublishedPage(Pageable pageable);

    /**
     * 依狀態與類別集合分頁查詢課程
     */
    Page<CourseBean> findPublishedPageByCategoryIds(List<Long> ids, Pageable pageable);

    /**
     * 依狀態與名稱關鍵字分頁查詢課程
     */
    Page<CourseBean> findPublishedPageByName(String kw, Pageable pageable);

    /**
     * 依狀態、類別集合與名稱關鍵字分頁查詢課程
     */
    Page<CourseBean> findPublishedPageByCategoryIdsAndName(List<Long> ids, String kw, Pageable pageable);

    /**
     * 統一搜尋（關鍵字、類別、狀態、價格區間）
     */
    Page<CourseBean> search(String keyword, Long categoryId, CourseStatus status,
                            Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * 更新課程狀態
     */
    CourseBean updateStatus(Long id, CourseStatus status);

    /**
     * 依講師名稱查詢課程
     */
    List<CourseBean> findByInstructorName(String instructorName);
}
