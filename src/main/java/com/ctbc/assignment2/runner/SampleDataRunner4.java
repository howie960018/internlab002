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

        // 利用 Repo 計數目前的課程總數量
        long beforeCount = courseRepo.count();
        System.out.println("刪除前課程總數：" + beforeCount);

        // 動作進行：直接將母分類給刪除
        categoryRepo.deleteById(cat.getId());

        // 預期底下原本掛著的 課程X 與 課程Y 都會隨之陪葬消滅
        long afterCount = courseRepo.count();
        System.out.println("✅ cascade：刪除類別後課程總數：" + afterCount);
    }
}
