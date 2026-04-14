package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import java.util.List;

/**
 * 課程類別服務介面 (Service Interface)
 * 
 * - 架構設計：
 *   將業務邏輯 (Business Logic) 放在 Service 層是非常重要的系統架構設計。
 *   藉由定義介面，提供統一的操作窗口與規範供 Controller 層呼叫。
 */
public interface CourseCategoryBeanService {
    
    /**
     * 查詢所有課程類別
     */
    List<CourseCategoryBean> findAll();

    /**
     * 根據 ID 尋找單一課程類別
     */
    CourseCategoryBean findById(Long id);

    /**
     * 儲存課程類別（處理新增與修改）
     */
    CourseCategoryBean save(CourseCategoryBean category);

    /**
     * 根據 ID 刪除課程類別
     */
    void deleteById(Long id);
}
