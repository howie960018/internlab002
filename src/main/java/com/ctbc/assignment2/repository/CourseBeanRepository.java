package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseBean;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;


public interface CourseBeanRepository
        extends JpaRepository<CourseBean, UUID> {

    // 新增時使用
    //：在「新增」資料時，問資料庫：「這個名稱是不是已經存在了？」
    boolean existsByCourseName(String courseName);

    // 更新時使用（排除自己）
    // 假設要把課程 A 改名為「Java 基礎」。系統會去檢查有沒有其他課程也叫「Java 基礎」，
    // 但必須排除掉自己 (IdNot)。否則系統會以為名稱重複而阻止你更新原本的資料
    boolean existsByCourseNameAndIdNot(String courseName, UUID id);

    // 依分類查詢課程
    // 撈出某個「類別 ID」底下的所有課程清單
    List<CourseBean> findByCategoryId(UUID categoryId);
}
