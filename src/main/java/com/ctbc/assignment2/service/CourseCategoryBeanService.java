package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import java.util.List;

/**
 * 課程分類服務介面 (Service Interface)
 * 在這裡我們定義了「課程分類管理」需要提供哪些功能 (如：尋找所有、依ID尋找、儲存、刪除)。
 * 透過 Service 層來封裝商業邏輯 (Business Logic)，讓存取資料庫的責任不要直接寫在 Controller 裡面。
 */
public interface CourseCategoryBeanService {

    // 找出目前系統中所有的課程分類
    List<CourseCategoryBean> findAll();

    // 根據分類的 ID 找出特定的分類
    CourseCategoryBean findById(Long id);

    // 儲存課程分類 (這方法會同時處理「新增」這個分類或是「修改」這個分類)
    CourseCategoryBean save(CourseCategoryBean category);

    // 根據分類的 ID 刪除特定的分類
    void deleteById(Long id);
}
