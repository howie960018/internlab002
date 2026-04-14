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

        // set_null：先把 category 設為 null 再刪除類別
        course.setCategory(null);
        courseRepo.save(course);
        categoryRepo.deleteById(cat.getId());

        System.out.println("✅ set_null：課程還在，category 變成 null");
        System.out.println("課程名稱：" + courseRepo.findById(course.getId()).get().getCourseName());
        System.out.println("課程類別：" + courseRepo.findById(course.getId()).get().getCategory());
    }
}
