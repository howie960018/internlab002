package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.service.CourseBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service 實作層 (Implementation)
 * 
 * - @Service: 這是 Spring 提供的一種標註，告訴 Spring 這個類別是服務層元件。
 *   在系統啟動時，Spring 會將他實例化 (Bean) 並放入容器中統一管理，我們之後便可透過 @Autowired 注入此元件。
 * - 介面繼承 (implements): 確保該類別實作了 CourseBeanService 介面定義的所有方法，落實規格和實作分離。
 * - @Transactional (常放在更新/刪除方法前): 用於控制資料庫事務 (Database Transaction)。
 *   確保「同一個方法內的資料操作要嘛全部成功，要嘛全部失敗退回原狀」，避免發生意外狀況導致一半資料變更的情況。
 */
@Service
public class CourseBeanServiceJPAImplement implements CourseBeanService {

    /**
     * @Autowired: 依賴注入 (Dependency Injection, DI)。
     * 我們不需要自己 new 建立 repository 物件，Spring 會自動將對應的 Repository Bean 配置進來。
     */
    @Autowired
    private CourseBeanRepository repo;

    /**
     * @Override: 明確指稱這是覆寫介面中的方法。幫助編譯器與開發者驗證方法的確存在於基礎介面中。
     */
    @Override
    public List<CourseBean> findAll() {
        return repo.findAll();
    }

    /**
     * 例外處理 (Exception Handling): 
     * 在找不到資源時拋出自訂例外 (ResourceNotFoundException)。
     * 這種設計可讓更外層 (Controller/GlobalExceptionHandler) 捕捉並轉成合適的 HTTP 狀態碼回傳給前端。
     */
    @Override
    public CourseBean findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    /**
     * 進行資料的存檔與更新
     * 
     * 我們常在這種會修改資料的方法加上 @Transactional 以確保事務的完整性（此專案範例預設採用 Spring Data JPA 的基礎交易防護）。
     * 在這裡加上許多業務邏輯檢查（例如名稱是否重複），發生重複時拋出例外讓前端得知錯誤訊息。
     */
    @Transactional
    @Override
    public CourseBean save(CourseBean course) {
        if (course.getId() != null) {
            // 更新：排除自身檢查是否重複
            if (repo.existsByCourseNameAndIdNot(course.getCourseName(), course.getId())) {
                throw new DuplicateCourseNameException("課程名稱已存在：" + course.getCourseName());
            }
        } else {
            // 新增：檢查是否已存在同名
            if (repo.existsByCourseName(course.getCourseName())) {
                throw new DuplicateCourseNameException("課程名稱已存在：" + course.getCourseName());
            }
        }
        return repo.save(course);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
