package com.ctbc.assignment2.runner;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 啟動前置程式 1：負責在一開始建立「預設的課程分類 (Category)」資料。
 * @Component 讓 Spring 啟動時掃描並建立這個物件
 * @Order(1) 表示這是整個專案中，啟動時第一個要執行的 Runner
 */
@Component
@Order(1)
public class SampleDataRunner2 implements CommandLineRunner {

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    /**
     * 當 Spring Boot 應用程式啟動完成後，會自動呼叫這個 run 方法。
     */
    @Override
    public void run(String... args) throws Exception {

        // 建立三個預設的類別並寫入資料庫
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("程式設計");
        categoryRepo.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("資料庫");
        categoryRepo.save(cat2);

        CourseCategoryBean cat3 = new CourseCategoryBean();
        cat3.setCategoryName("網頁開發");
        categoryRepo.save(cat3);

        // 印出 log 讓開發者知道現在有幾筆分類資料
        System.out.println("✅ SampleDataRunner2：類別總數 = " + categoryRepo.count());
        categoryRepo.findAll().forEach(cat ->
            System.out.println("  類別：" + cat.getId() + " / " + cat.getCategoryName())
        );
    }
}
