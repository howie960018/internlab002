package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;

import jakarta.websocket.MessageHandler.Partial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


// 加上 @Service 讓 Spring Boot 知道這是一個服務層元件，並將它納入管理
@Service
public class CourseBeanServiceJPAImplement implements CourseBeanService {

    @Autowired
    private CourseBeanRepository repo;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @Override
    public List<CourseBean> findAll() {
        return repo.findAll(); // 呼叫 Repository 撈取所有課程
    }

    @Override
    public CourseBean findById(UUID id) {
        // 🌟 防呆機制：使用 orElseThrow
        // 如果找不到這個 ID 的課程，直接拋出「找不到資源」的錯誤，避免後續發生 NullPointerException
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found: " + id));
    }

    // @Override
    // public CourseBean save(CourseBean course) {

    //     // 🌟 核心邏輯：判斷目前是「更新」還是「新增」
    //     if (course.getId() != null) {
    //         // 情況 A：更新 (因為有傳入 ID)
    //         // 🌟 檢查：修改後的名稱是否跟「別人」重複了？(排除掉自己原本的 ID)
    //         if (repo.existsByCourseNameAndIdNot(
    //                 course.getCourseName(), course.getId())) {
    //             throw new DuplicateCourseNameException(
    //                     "課程名稱已存在：" + course.getCourseName());
    //         }
    //     } else {
    //         // 情況 B：新增 (因為沒有 ID，代表是全新的資料)
    //         // 🌟 檢查：資料庫裡是不是已經有這個課程名稱了？
    //         if (repo.existsByCourseName(course.getCourseName())) {
    //             throw new DuplicateCourseNameException(
    //                     "課程名稱已存在：" + course.getCourseName());
    //         }
    //     }

    //     // 通過所有驗證後，才安心存入資料庫
    //     return repo.save(course);
    // }


    //不直接信任前端傳來的整包資料。它先用 ID 去資料庫把舊資料 (existing) 完整地撈出來
    // 接著，只把允許修改的欄位（例如名稱）從新資料複製到舊資料上，最後把更新後的資料存回去。
    //這樣可確保其他不需要修改的欄位（如建立時間、關聯的課程清單等）不會因為前端漏傳而被意外清空。這叫「部分更新 (Partial Update)」
    @Override
    public CourseBean save(CourseBean course) {

    // 情況 A：更新
    if (course.getId() != null) {

        // 1. 先把資料庫裡的舊資料撈出來
        CourseBean existing = repo.findById(course.getId())
                .orElseThrow(() -> 
                        new ResourceNotFoundException("Course not found: " + course.getId()));

        // 2. 檢查新名字有沒有跟別的課程重複
        if (repo.existsByCourseNameAndIdNot(course.getCourseName(), course.getId())) {
            throw new DuplicateCourseNameException(
                    "課程名稱已存在：" + course.getCourseName());
        }

        // 3. 安全更新：只將允許前端修改的欄位設定到 existing 物件上
        existing.setCourseName(course.getCourseName());
        existing.setPrice(course.getPrice());
        
        // 如果允許修改分類，也可以在這裡更新
        if (course.getCategory() != null) {
            existing.setCategory(course.getCategory());
        }

        return repo.save(existing);
    } 
    
    // 情況 B：新增
    else {
        if (repo.existsByCourseName(course.getCourseName())) {
            throw new DuplicateCourseNameException(
                    "課程名稱已存在：" + course.getCourseName());
        }
        return repo.save(course);
    }
}

    @Override
    public void deleteById(UUID id) {
        // 先確定課程存在，再執行刪除
        CourseBean course = findById(id);
        repo.delete(course);
    }

    @Override
    public List<CourseBean> findByCategoryId(UUID categoryId) {
        // 🌟 貼心設計 (Fail-Fast)：
        // 在去資料庫搜尋課程前，先檢查這個「類別」到底存不存在。
        // 如果類別不存在，就提早拋出錯誤被擋下，不會浪費效能去資料庫做白工
        categoryService.findById(categoryId); 
        return repo.findByCategoryId(categoryId);
    }
}