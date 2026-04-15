package com.ctbc.assignment2.runner;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SampleDataRunner2 implements CommandLineRunner {

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    @Override
    public void run(String... args) throws Exception {

        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("軟體工程");
        categoryRepo.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("資料庫");
        categoryRepo.save(cat2);

        CourseCategoryBean cat3 = new CourseCategoryBean();
        cat3.setCategoryName("網頁開發");
        categoryRepo.save(cat3);

        System.out.println("✅ SampleDataRunner2：類別總數 = " + categoryRepo.count());
        categoryRepo.findAll().forEach(cat ->
            System.out.println("  類別：" + cat.getId() + " / " + cat.getCategoryName())
        );
    }
}
