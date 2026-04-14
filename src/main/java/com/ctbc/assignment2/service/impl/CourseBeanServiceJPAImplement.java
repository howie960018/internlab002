package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.service.CourseBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 課程服務的具體實作類別 (Service Implementation)
 * 利用 @Service 將這個類別交由 Spring 管理 (產生 Bean)。
 * 裡面處理了這專案所有「課程」的核心商務邏輯，比如確保避免出現重複的課程名稱。
 */
@Service
public class CourseBeanServiceJPAImplement implements CourseBeanService {

    // 自動把 CourseBeanRepository (與資料庫溝通的組件) 借過來用
    @Autowired
    private CourseBeanRepository repo;

    /**
     * 從資料庫中查出所有的課程資料。
     */
    @Override
    public List<CourseBean> findAll() {
        return repo.findAll();
    }

    /**
     * 根據給的課程 ID 去資料庫找課程。
     * 如果找到了就回傳課程物件；如果找不到，就拋出我們自己設計好的 ResourceNotFoundException 例外。
     */
    @Override
    public CourseBean findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    /**
     * 儲存一筆課程資料，根據傳進來的課程「是否有 ID」來決定是「新增」還是「更新」。
     * 並在此處把關：不管是哪種情況，都不允許有重複名稱的課程存進資料庫中 (會拋出 DuplicateCourseNameException)。
     */
    @Override
    public CourseBean save(CourseBean course) {
        if (course.getId() != null) {
            // == 情況A：有 ID，代表現在正在進行「更新 (Update)」現有的課程 == 
            
            // 去資料庫檢查：這個改的新名字，是不是已經被別的課程(別人的ID)用走了？
            if (repo.existsByCourseNameAndIdNot(course.getCourseName(), course.getId())) {
                throw new DuplicateCourseNameException("課程名稱已存在：" + course.getCourseName()); // 如果有，終止儲存，並告訴用戶這名字不能用
            }
        } else {
            // == 情況B：沒有 ID，代表這是一門「全新 (Create)」的課程 ==
            
            // 去資料庫檢查：以前是不是早就有人建立過一模一樣名稱的課程了？
            if (repo.existsByCourseName(course.getCourseName())) {
                throw new DuplicateCourseNameException("課程名稱已存在：" + course.getCourseName());
            }
        }
        
        // 成功經過檢查！請 Repository 放心地把這筆資料存進/更新到資料庫中
        return repo.save(course);
    }

    /**
     * 刪除課程。直接委派給 Repository 的 deleteById 做苦力活。
     */
    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
