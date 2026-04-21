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
 * 啟動前置程式 3：驗證當某個類別要把課程的關聯解除(設為 null) 時，孤兒資料的狀態。
 * 這用來測試 Spring Data JPA 進行關聯清理 (Set Null) 的行為。
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

        categoryRepo.deleteById(cat.getId());

        System.out.println("✅ 自動 set null 測試 (Runner 3)");
        CourseBean found = courseRepo.findById(course.getId()).orElse(null);
        if (found != null) {
            System.out.println("課程名稱：" + found.getCourseName());
            System.out.println("課程類別：" +
                    (found.getCategory() == null ? "null" : found.getCategory().getCategoryName()));
        }
    }
}