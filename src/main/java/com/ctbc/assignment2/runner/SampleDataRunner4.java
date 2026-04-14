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
@Order(4)
public class SampleDataRunner4 implements CommandLineRunner {

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    @Override
    public void run(String... args) throws Exception {

        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("連帶刪除類別");
        categoryRepo.save(cat);

        CourseBean course1 = new CourseBean();
        course1.setCourseName("課程X");
        course1.setPrice(200.0);
        course1.setCategory(cat);
        courseRepo.save(course1);

        CourseBean course2 = new CourseBean();
        course2.setCourseName("課程Y");
        course2.setPrice(300.0);
        course2.setCategory(cat);
        courseRepo.save(course2);

        long beforeCount = courseRepo.count();
        System.out.println("刪除前課程總數：" + beforeCount);

        categoryRepo.deleteById(cat.getId());

        long afterCount = courseRepo.count();
        System.out.println("✅ cascade：刪除類別後課程總數：" + afterCount);
    }
}
