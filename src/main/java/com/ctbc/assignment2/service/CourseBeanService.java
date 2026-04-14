package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseBean;
import java.util.List;
import java.util.UUID;

/**
 * 課程服務介面 (Service Interface)
 * 在這裡我們定義了「課程管理」需要提供哪些功能 (如：尋找所有、依ID尋找、儲存、刪除)。
 * 使用介面 (Interface) 的好處是可以把「定義」跟「實作」分開，未來如果換了資料庫或是實作方式，
 * 只需要在實作類別修改，不會影響到呼叫這介面的 Controller。
 */
public interface CourseBeanService {
    
    // 找出目前系統中所有的課程
    List<CourseBean> findAll();
    
    // 根據課程的 ID 找出特定的課程
    CourseBean findById(UUID id);
    
    // 儲存課程 (包含「新增」與「修改」)
    CourseBean save(CourseBean course);
    
    // 根據課程的 ID 刪除特定課程
    void deleteById(UUID id);
}
