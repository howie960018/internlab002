package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 課程分類服務的具體實作類別 (Service Implementation)
 * 利用 @Service 宣告為服務層，Spring 啟動時會幫我們建立實體 (DI)，
 * 這邊實作負責處理所有關於「分類(Category)」的保護機制與存取邏輯。
 */
@Service
public class CourseCategoryBeanServiceJPAImplement implements CourseCategoryBeanService {

    // 依賴注入 (DI) CourseCategoryBeanRepository，由它代理與資料庫互動的工作
    @Autowired
    private CourseCategoryBeanRepository repo;

    /**
     * 從資料庫中查出所有的類別。
     */
    @Override
    public List<CourseCategoryBean> findAll() {
        return repo.findAll();
    }

    /**
     * 從資料庫找定特定 ID 的類別。
     * 若遇到找不到的狀況，避免回傳可能會拋出 Null 機率的 null，我們故意拋出自訂的 ResourceNotFoundException 異常。
     */
    @Override
    public CourseCategoryBean findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    /**
     * 負責將控制器傳來的類別資料進行「建立新增」或者「內容更新」。
     */
    @Override
    public CourseCategoryBean save(CourseCategoryBean category) {
        
        // 若該這筆傳來的資料上面帶有 ID ，代表這是使用者選擇做「更新(Update)」
        if (category.getId() != null) {
            
            // 【重要】更新時，先去資料庫將現存的"這筆原本的類別資料"調閱出來：
            // 為什麼要多這步呢？這是因為分類本身可能有牽連著底下好幾門「課程 (courses) 的集合」。
            // 如果我們偷懶直接存入前端傳回來的新物件，原本該分類底下綁定的課程可能就會被完全洗掉。
            CourseCategoryBean existing = repo.findById(category.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + category.getId()));
            
            // 檢查：要改的名字是不是碰巧跟別人重複了？若是跟原本的自己(ID一樣)重複則不算
            if (repo.existsByCategoryNameAndIdNot(category.getCategoryName(), category.getId())) {
                throw new DuplicateCourseNameException("類別名稱已存在：" + category.getCategoryName());
            }
            
            // 只把我們「允許被更新的屬性」 (例如名稱) 套用到從資料庫拿出的那個安全物件上
            existing.setCategoryName(category.getCategoryName());
            
            // 存入最完整安全的 existing 物件，不會害那些底下綁定的課程跑掉
            return repo.save(existing);
        }
        
        // ========================================================
        // 若該筆傳來的資料沒有 ID，代表將它是全新建立「新增 (Create)」
        
        // 檢查：以前是不是早就有人建立過一模一樣名稱的分類了？
        if (repo.existsByCategoryName(category.getCategoryName())) {
            throw new DuplicateCourseNameException("類別名稱已存在：" + category.getCategoryName());
        }
        // 如果這個名字全新沒人用過，那就建立存檔！
        return repo.save(category);
    }

    /**
     * 將指定的分類，由資料庫驅逐出境。
     */
    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
