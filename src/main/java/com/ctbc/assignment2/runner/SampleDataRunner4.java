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
 * 啟動前置程式 4：負責測試實體驗證「串聯刪除 (Cascade Delete)」功能。
 * 由於在 CourseCategoryBean 實體類有設定了 cascade = CascadeType.ALL 與 orphanRemoval = true
 * 因此當我們刪除了分類後，底下掛著的子課程將如預期中一併被牽連著自動刪除掉。
 */
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
        cat.setCategoryName("連帶變成 null 類別 (Runner 4)");
        categoryRepo.save(cat);

        CourseBean course1 = new CourseBean();
        course1.setCourseName("課程 X");
        course1.setPrice(200.0);
        course1.setCategory(cat);
        courseRepo.save(course1);

        CourseBean course2 = new CourseBean();
        course2.setCourseName("課程 Y");
        course2.setPrice(300.0);
        course2.setCategory(cat);
        courseRepo.save(course2);

        System.out.println("====== [Runner 4] 刪除前 ======");
        System.out.println("目前課程總數：" + courseRepo.count());

        categoryRepo.deleteById(cat.getId());

        System.out.println("====== [Runner 4] 刪除後 ======");
        System.out.println("✅ 刪除類別後課程總數：" + courseRepo.count());

        CourseBean foundX = courseRepo.findById(course1.getId()).orElse(null);
        CourseBean foundY = courseRepo.findById(course2.getId()).orElse(null);

        if (foundX != null) {
            System.out.println("課程 X 的類別變成：" +
                    (foundX.getCategory() == null ? "null" : foundX.getCategory().getCategoryName()));
        }
        if (foundY != null) {
            System.out.println("課程 Y 的類別變成：" +
                    (foundY.getCategory() == null ? "null" : foundY.getCategory().getCategoryName()));
        }
        System.out.println("==============================================");
    }
}