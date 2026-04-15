package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.CategoryHierarchyException;
import com.ctbc.assignment2.exception.CategoryNotEmptyException;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 課程類別 Service 實作層
 * 
 * - 設計概念：這裡是系統架構的「業務邏輯層」。接收 Controller 傳來的需求，並操作 Repository (資料訪問層) 來取得/修改資料。
 * - @Service: 類別層級的標註。讓 Spring 在啟動時能掃描到這個類別，產生可以被 @Autowired 注入的單例 (Singleton) Bean。
 */
@Service
public class CourseCategoryBeanServiceJPAImplement implements CourseCategoryBeanService {

    /**
     * @Autowired: 自動注入相依物件。
     * 將 CourseCategoryBeanRepository 提供的方法 (findAll, save 等) 讓這支 Service 操作。
     */
    @Autowired
    private CourseCategoryBeanRepository repo;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Override
    public List<CourseCategoryBean> findAll() {
        return repo.findAll();
    }

    @Override
    public CourseCategoryBean findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    public List<CourseCategoryBean> findTopLevel() {
        return repo.findByParentIsNullOrderByCategoryName();
    }

    @Override
    public List<CourseCategoryBean> findChildren(Long parentId) {
        return repo.findByParentIdOrderByCategoryName(parentId);
    }

    /**
     * 進行類別的修改/新增。
     * - @Transactional: 保障資料庫讀寫的事務完整性 (Transaction)。
     *   方法執行中若拋出 RuntimeException 例外，此次操作的資料將全部回到執行前的狀態 (Rollback)，避免只有部分更新。
     */
    @Transactional
    @Override
    public CourseCategoryBean save(CourseCategoryBean category) {
        CourseCategoryBean parent = null;
        if (category.getParent() != null && category.getParent().getId() != null) {
            Long parentId = category.getParent().getId();
            if (category.getId() != null && parentId.equals(category.getId())) {
                throw new CategoryHierarchyException("父類別不可為自己");
            }
                    parent = repo.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("父類別不存在: " + parentId));
            if (parent.getParent() != null) {
                throw new CategoryHierarchyException("父類別必須是主類別");
            }
        }

        if (category.getId() != null) {
            // 更新：載入既有實體保留 courses 集合，排除自身檢查重複名稱
            CourseCategoryBean existing = repo.findById(category.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + category.getId()));
            if (repo.existsByCategoryNameAndIdNot(category.getCategoryName(), category.getId())) {
                throw new DuplicateCourseNameException("類別名稱已存在：" + category.getCategoryName());
            }
            if (parent != null && repo.existsByParentId(existing.getId())) {
                throw new CategoryHierarchyException("已有子類別的主類別不可改為子類別");
            }
            existing.setCategoryName(category.getCategoryName());
            existing.setParent(parent);
            return repo.save(existing);
        }
        // 新增：檢查是否已存在同名
        if (repo.existsByCategoryName(category.getCategoryName())) {
            throw new DuplicateCourseNameException("類別名稱已存在：" + category.getCategoryName());
        }
        category.setParent(parent);
        return repo.save(category);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        if (repo.existsByParentId(id)) {
            throw new CategoryNotEmptyException("此類別底下仍有子類別，請先移除或轉移");
        }
        if (courseRepo.existsByCategoryId(id)) {
            throw new CategoryNotEmptyException("此類別底下仍有課程，請先移除或轉移");
        }
        repo.deleteById(id);
    }
}
