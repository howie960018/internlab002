package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest: Spring Boot 提供針對 JPA Repository 進行的切片測試 (Slice test)。
// 自動加態 In-memory DB，不載入 Controller 等元件，各 @Test 結束會 rollback 保持環境乾淨。
@DataJpaTest
public class CourseCategoryBeanTest {

    @Autowired
    private TestEntityManager em; // 提供更底層的 DB 管理器工具 (如 flush, clear) 來精準控制測試

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // @Test: JUnit 測試方法標註
    @Test
    public void testSaveCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Java");
        categoryRepo.save(cat);

        // 【修正】flush + clear 確保查詢來自資料庫，而非一級快取
        em.flush();
        em.clear();

        assertThat(categoryRepo.findAll()).hasSize(1);
        assertThat(categoryRepo.findAll().get(0).getCategoryName()).isEqualTo("Java");
        System.out.println("✅ testSaveCategory 通過");
    }

    @Test
    public void testAddCourseToCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Web");
        categoryRepo.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("HTML 入門");
        course.setPrice(1000.0);
        course.setCategory(cat);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        assertThat(found.getCategory().getCategoryName()).isEqualTo("Web");
        System.out.println("✅ testAddCourseToCategory 通過");
    }

    @Test
    public void testMoveCourseToAnotherCategory() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("類別1");
        categoryRepo.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("類別2");
        categoryRepo.save(cat2);

        CourseBean course = new CourseBean();
        course.setCourseName("測試課程");
        course.setPrice(500.0);
        course.setCategory(cat1);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        CourseCategoryBean newCat = categoryRepo.findById(cat2.getId()).get();
        found.setCategory(newCat);
        courseRepo.save(found);

        em.flush();
        em.clear();

        CourseBean updated = courseRepo.findById(course.getId()).get();
        assertThat(updated.getCategory().getCategoryName()).isEqualTo("類別2");
        System.out.println("✅ testMoveCourseToAnotherCategory 通過");
    }

    @Test
    public void testRemoveCategoryFromCourse_setNull() {
        // 【新增】將已有類別的課程設為 null
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("可移除類別");
        categoryRepo.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("可移除類別課程");
        course.setPrice(300.0);
        course.setCategory(cat);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        found.setCategory(null);
        courseRepo.save(found);

        em.flush();
        em.clear();

        assertThat(courseRepo.findById(course.getId()).get().getCategory()).isNull();
        // 類別本身仍存在
        assertThat(categoryRepo.findById(cat.getId())).isPresent();
        System.out.println("✅ testRemoveCategoryFromCourse_setNull 通過");
    }

    @Test
    public void testCategoryExistsByCategoryNameTrue() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("存在類別名稱");
        categoryRepo.save(cat);
        em.flush();

        assertThat(categoryRepo.existsByCategoryName("存在類別名稱")).isTrue();
        System.out.println("✅ testCategory_existsByCategoryName_true 通過");
    }

    @Test
    public void testCategoryExistsByCategoryNameFalse() {
        assertThat(categoryRepo.existsByCategoryName("完全不存在XYZ")).isFalse();
        System.out.println("✅ testCategory_existsByCategoryName_false 通過");
    }

    @Test
    public void testCategoryExistsByCategoryNameAndIdNotExcludeSelfFalse() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("唯一類別名");
        categoryRepo.save(cat);
        em.flush();

        assertThat(categoryRepo.existsByCategoryNameAndIdNot("唯一類別名", cat.getId())).isFalse();
        System.out.println("✅ testCategory_existsByCategoryNameAndIdNot_排除自身回false 通過");
    }

    @Test
    public void testCategoryExistsByCategoryNameAndIdNotExcludeOtherTrue() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("重複類別名");
        categoryRepo.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("另一類別");
        categoryRepo.save(cat2);
        em.flush();

        assertThat(categoryRepo.existsByCategoryNameAndIdNot("重複類別名", cat2.getId())).isTrue();
        System.out.println("✅ testCategory_existsByCategoryNameAndIdNot_排除他人回true 通過");
    }
}