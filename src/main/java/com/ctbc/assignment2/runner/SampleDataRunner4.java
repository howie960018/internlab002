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
 * 這是幫我們寫入多筆測試資料的 Runner。
 * 同時用以驗證：當我們刪除一個「類別」時，如果這個類別底下有很多「課程」，
 * 底下的課程應該不會被連帶刪除，而是把課程對應的類別 (外鍵) 自動改成 null (透過 @PreRemove)。
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

        // 1. 建立一個新類別並寫入資料庫
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("連帶變成 null 類別 (Runner 4)");
        // 為了讓 JPA 的 @OneToMany (mappedBy="category") 在兩邊都擁有關聯，這裡建立與課程的多對一連接
        categoryRepo.save(cat);

        // 2. 建立課程X，並且設置關聯
        CourseBean course1 = new CourseBean();
        course1.setCourseName("課程X");
        course1.setPrice(200.0);
        course1.setCategory(cat);
        courseRepo.save(course1);

        // 3. 建立課程Y，並且設置關聯
        CourseBean course2 = new CourseBean();
        course2.setCourseName("課程Y");
        course2.setPrice(300.0);
        course2.setCategory(cat);
        courseRepo.save(course2);

        long beforeCount = courseRepo.count();
        System.out.println("====== [Runner 4] 刪除前 ======");
        System.out.println("目前課程總數：" + beforeCount);

        // 4. 因為我們先前已經移除了 CascadeType.ALL 並改用 @PreRemove，
        // 這裡直接刪除類別看看會發生什麼事。
        categoryRepo.deleteById(cat.getId());

        long afterCount = courseRepo.count();
        System.out.println("====== [Runner 4] 刪除後 ======");
        System.out.println("✅ 刪除類別後課程總數：" + afterCount);
        
        // 找出被保留的課程，印出他們的狀態來驗證
        CourseBean foundCourse1 = courseRepo.findById(course1.getId()).orElse(null);
        CourseBean foundCourse2 = courseRepo.findById(course2.getId()).orElse(null);

        if (foundCourse1 != null) {
            String catName = foundCourse1.getCategory() == null ? "null" : foundCourse1.getCategory().getCategoryName();
            System.out.println("課程X 的類別變成：" + catName);
        }
        if (foundCourse2 != null) {
            String catName = foundCourse2.getCategory() == null ? "null" : foundCourse2.getCategory().getCategoryName();
            System.out.println("課程Y 的類別變成：" + catName);
        }
        System.out.println("=================================");
    }
}
