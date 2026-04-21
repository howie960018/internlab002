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
@Order(0)
public class SampleDataRunnable implements CommandLineRunner {

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("------建立課程並關聯類別------");

        CourseCategoryBean java = new CourseCategoryBean();
        java.setCategoryName("Java");
        categoryRepo.save(java);

        CourseCategoryBean web = new CourseCategoryBean();
        web.setCategoryName("Web");
        categoryRepo.save(web);
        
        CourseBean course1= new CourseBean();
        course1.setCourseName("Java入門");
        course1.setPrice(2000.0);
        course1.setCategory(java);
        courseRepo.save(course1);
        
        CourseBean course2= new CourseBean();
        course2.setCourseName("React入門");
        course2.setPrice(2000.0);
        course2.setCategory(web);
        courseRepo.save(course2);

        CourseBean course3 = new CourseBean();
        course3.setCourseName("HTML/CSS 入門");
        course3.setPrice(2000.0);
        course3.setCategory(web);
        courseRepo.save(course3);

        CourseBean course4 = new CourseBean();
        course4.setCourseName("C# 初階課程");
        course4.setPrice(4000.0);
        courseRepo.save(course4);

        CourseBean course5 = new CourseBean();
        course5.setCourseName("C# 進階課程");
        course5.setPrice(5000.0);
        courseRepo.save(course5);

        System.out.println("預設資料已寫入資料庫");

        // 驗證 updatedAt
        System.out.println("===== 驗證 updatedAt =====");
        CourseBean testCourse = new CourseBean();
        testCourse.setCourseName("驗證用課程");
        testCourse.setPrice(999.0);
        courseRepo.save(testCourse);
        System.out.println("儲存後 updatedAt : " + testCourse.getUpdatedAt());

        Thread.sleep(2000);

        testCourse.setCourseName("驗證用課程 (已修改)");
        testCourse.setPrice(1999.0);
        courseRepo.save(testCourse);
        System.out.println("修改後 updatedAt : " + testCourse.getUpdatedAt());
    }
}