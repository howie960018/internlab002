package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseCategoryBeanServiceJPAImplement
        implements CourseCategoryBeanService {

    @Autowired
    private CourseCategoryBeanRepository repo;

    @Override
    public List<CourseCategoryBean> findAll() {
        return repo.findAll();
    }

    @Override
    public CourseCategoryBean findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    public CourseCategoryBean save(CourseCategoryBean category) {

        // 情況 A：更新既有分類
        if (category.getId() != null) {
            // 🌟 JPA 標準更新流程：
            // 1. 先把「舊資料」從資料庫撈出來 (existing)
            CourseCategoryBean existing =
                    repo.findById(category.getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Category not found: " + category.getId()));

            // 2. 檢查新名字有沒有跟別的類別重複
            if (repo.existsByCategoryNameAndIdNot(
                    category.getCategoryName(), category.getId())) {
                throw new DuplicateCourseNameException(
                        "類別名稱已存在：" + category.getCategoryName());
            }

            // 🌟 3. 只更新我們允許修改的欄位 (這裡只有名稱)
            // 這樣能避免前端亂傳資料，把建立時間(createdAt)或其他不該改的欄位給覆蓋掉！
            existing.setCategoryName(category.getCategoryName());
            return repo.save(existing);
        }

        // 情況 B：新增分類
        if (repo.existsByCategoryName(category.getCategoryName())) {
            throw new DuplicateCourseNameException(
                    "類別名稱已存在：" + category.getCategoryName());
        }

        return repo.save(category);
    }

    // 🌟確保這整個方法是一個「不可分割的交易」
    // 如果執行到一半出錯，資料庫會全部還原 (Rollback)，不會留下錯誤的資料！
    @Override
    @Transactional
    public void deleteById(UUID id) {
        CourseCategoryBean category = findById(id);

        // 🌟 雙重保險 (記憶體層級的關聯切斷)：
        // 雖然 Entity 有設定 @OnDelete(SET_NULL)，但為了確保目前的 Java 程式狀態正確，
        // 我們手動將這個類別底下的所有課程的 category 設為 null，讓它們變成「未分類」狀態。
        if (category.getCourses() != null) {
            for (CourseBean course : category.getCourses()) {
                course.setCategory(null);
            }
        }

        repo.delete(category); // 最後才把類別本人刪除
    }
}