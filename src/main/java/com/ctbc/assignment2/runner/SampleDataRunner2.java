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
        createCategoryIfMissing("軟體工程");
        createCategoryIfMissing("資料庫");
        createCategoryIfMissing("網頁開發");

        System.out.println("✅ SampleDataRunner2：類別總數 = " + categoryRepo.count());
        categoryRepo.findAll().forEach(cat ->
            System.out.println("  類別：" + cat.getId() + " / " + cat.getCategoryName())
        );
    }

    private void createCategoryIfMissing(String name) {
        if (categoryRepo.existsByCategoryName(name)) {
            return;
        }
        CourseCategoryBean category = new CourseCategoryBean();
        category.setCategoryName(name);
        categoryRepo.save(category);
    }
}
