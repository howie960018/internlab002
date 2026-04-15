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
@Order(5)
public class SampleDataRunner5 implements CommandLineRunner {

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    @Override
    public void run(String... args) throws Exception {
        CourseCategoryBean arts = new CourseCategoryBean();
        arts.setCategoryName("藝術");
        categoryRepo.save(arts);

        CourseCategoryBean music = new CourseCategoryBean();
        music.setCategoryName("音樂");
        music.setParent(arts);
        categoryRepo.save(music);

        CourseCategoryBean painting = new CourseCategoryBean();
        painting.setCategoryName("繪畫");
        painting.setParent(arts);
        categoryRepo.save(painting);

        CourseCategoryBean tech = new CourseCategoryBean();
        tech.setCategoryName("科技");
        categoryRepo.save(tech);

        CourseCategoryBean programming = new CourseCategoryBean();
        programming.setCategoryName("程式設計");
        programming.setParent(tech);
        categoryRepo.save(programming);

        CourseCategoryBean data = new CourseCategoryBean();
        data.setCategoryName("資料分析");
        data.setParent(tech);
        categoryRepo.save(data);

        CourseCategoryBean business = new CourseCategoryBean();
        business.setCategoryName("商業");
        categoryRepo.save(business);

        CourseCategoryBean marketing = new CourseCategoryBean();
        marketing.setCategoryName("行銷");
        marketing.setParent(business);
        categoryRepo.save(marketing);

        CourseCategoryBean finance = new CourseCategoryBean();
        finance.setCategoryName("財務");
        finance.setParent(business);
        categoryRepo.save(finance);

        createCourse("古典鋼琴入門", 2200.0, music);
        createCourse("吉他伴奏實戰", 1800.0, music);
        createCourse("爵士和聲進階", 2600.0, music);

        createCourse("素描基礎", 1500.0, painting);
        createCourse("水彩光影", 1700.0, painting);
        createCourse("數位插畫", 2400.0, painting);

        createCourse("Java 基礎", 3000.0, programming);
        createCourse("Spring Boot 專題", 4500.0, programming);
        createCourse("前端 React 開發", 3800.0, programming);

        createCourse("Python 資料分析", 3200.0, data);
        createCourse("SQL 報表實作", 2800.0, data);
        createCourse("可視化儀表板", 3500.0, data);

        createCourse("品牌行銷策略", 2600.0, marketing);
        createCourse("廣告投放實務", 2300.0, marketing);
        createCourse("內容行銷操作", 2100.0, marketing);

        createCourse("財務報表入門", 2500.0, finance);
        createCourse("預算規劃與控管", 2700.0, finance);
        createCourse("投資分析實戰", 3200.0, finance);

        for (int i = 1; i <= 20; i++) {
            createCourse("專題實作課程-" + i, 1500.0 + i * 50, programming);
        }

        System.out.println("✅ 已寫入階層類別與課程資料 (Runner 5)");
    }

    private void createCourse(String name, Double price, CourseCategoryBean category) {
        CourseBean course = new CourseBean();
        course.setCourseName(name);
        course.setPrice(price);
        course.setCategory(category);
        courseRepo.save(course);
    }
}
