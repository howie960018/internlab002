package com.ctbc.assignment2.runner;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 【Spring Boot 啟動時自動執行的 Runner】
 * 實作 CommandLineRunner，可以在 Spring Boot 啟動完成之後立刻執行裡面 run() 的內容。
 * 這是用來塞系統預設假資料 (Sample Data) 非常常用的方式。
 * 
 * @Order(3) 代表它的執行順序，數字越小越先啟動。
 */
@Component
@Order(3)
public class SampleDataRunner3 implements CommandLineRunner {

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    @Override
    public void run(String... args) throws Exception {

        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("待刪除類別");
        categoryRepo.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("孤兒課程");
        course.setPrice(100.0);
        course.setCategory(cat);
        courseRepo.save(course);

        // 現在由於我們已經在 CourseCategoryBean 寫了 @PreRemove 自動解除關聯
        // 所以直接刪除類別時，底下的課程就不會因為 Foreign Key (外鍵) 的限制而發生錯誤，
        // 而會自動把 category_id 變成 null。
        categoryRepo.deleteById(cat.getId());

        System.out.println("✅ 自動 set null 測試 (Runner 3)：課程還在，category 變成 null");
        System.out.println("課程名稱：" + courseRepo.findById(course.getId()).get().getCourseName());
        System.out.println("課程類別：" + courseRepo.findById(course.getId()).get().getCategory());
    }
}
