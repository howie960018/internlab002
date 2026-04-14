package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseBean;
import java.util.List;

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
}
