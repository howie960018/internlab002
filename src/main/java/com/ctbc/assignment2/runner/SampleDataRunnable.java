package com.ctbc.assignment2.runner;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SampleDataRunnable implements CommandLineRunner {

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    @Override
    public void run(String... args) throws Exception {

        // 建立課程並關聯類別（使用 SampleDataRunner2 建立的類別）
        CourseCategoryBean java = categoryRepo.findAll().get(0);
        CourseCategoryBean web = categoryRepo.findAll().get(2);

        CourseBean course1 = new CourseBean();
        course1.setCourseName("Java 基礎");
        course1.setPrice(3000.0);
        course1.setCategory(java);
        courseRepo.save(course1);

        CourseBean course2 = new CourseBean();
        course2.setCourseName("Spring Boot 入門");
        course2.setPrice(5000.0);
        course2.setCategory(java);
        courseRepo.save(course2);

        CourseBean course3 = new CourseBean();
        course3.setCourseName("HTML/CSS 入門");
        course3.setPrice(2000.0);
        course3.setCategory(web);
        courseRepo.save(course3);

        // 驗証 updatedAt 有被更新
        System.out.println("===== 驗証 updatedAt =====");
        CourseBean testCourse = new CourseBean();
        testCourse.setCourseName("驗証用課程");
        testCourse.setPrice(999.0);
        courseRepo.save(testCourse);
        System.out.println("儲存後 updatedAt：" + testCourse.getUpdatedAt());

        try { Thread.sleep(10); } catch (Exception e) {}

        testCourse.setCourseName("驗証用課程（已修改）");
        testCourse.setPrice(1999.0);
        courseRepo.save(testCourse);
        System.out.println("修改後 updatedAt：" + testCourse.getUpdatedAt());
        System.out.println("✅ 請去 h2-console 查看 COURSE 表確認 UPDATED_AT 欄位有更新");

        System.out.println("✅ 預設資料已寫入資料庫");
    }
}
