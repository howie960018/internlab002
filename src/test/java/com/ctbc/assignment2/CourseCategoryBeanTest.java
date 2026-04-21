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


/**
 * 課程分類 (CourseCategory) 相關 JPA 行為測試
 */
@DataJpaTest
public class CourseCategoryBeanTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    /**
     * 測試：是否能單純儲存一筆課程分類
     */
    @Test
    public void testSaveCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Java");

        categoryRepo.save(cat);

        em.flush();
        em.clear();

        assertThat(categoryRepo.findAll()).hasSize(1);
        assertThat(categoryRepo.findAll().get(0).getCategoryName()).isEqualTo("Java");

        System.out.println("✅ testSaveCategory 通過");
    }

    /**
     * 測試：建立課程並指定分類
     */
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

    /**
     * 測試：課程從 A 分類移動到 B 分類
     */
    @Test
    public void testMoveCourseToAnotherCategory() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("類別 1");
        categoryRepo.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("類別 2");
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
        assertThat(updated.getCategory().getCategoryName()).isEqualTo("類別 2");

        System.out.println("✅ testMoveCourseToAnotherCategory 通過");
    }

    /**
     * 測試：將課程從分類中移除 (將 category 關聯設為 null)
     */
    @Test
    public void testRemoveCategoryFromCourse_SetNull() {
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
        assertThat(categoryRepo.findById(cat.getId()).isPresent()).isTrue();

        System.out.println("✅ testRemoveCategoryFromCourse_SetNull 通過");
    }

    /**
     * 測試：分類名稱存在時，existsByCategoryName 回傳 true
     */
    @Test
    public void testExistsByCategoryName_ReturnsTrue() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Java");
        categoryRepo.save(cat);

        em.flush();

        assertThat(categoryRepo.existsByCategoryName("Java")).isTrue();

        System.out.println("✅ testExistsByCategoryName_ReturnsTrue 通過");
    }

    /**
     * 測試：分類名稱不存在時，existsByCategoryName 回傳 false
     */
    @Test
    public void testExistsByCategoryName_ReturnsFalse() {
        assertThat(categoryRepo.existsByCategoryName("Python")).isFalse();

        System.out.println("✅ testExistsByCategoryName_ReturnsFalse 通過");
    }

    /**
     * 測試：檢查名稱是否重複（排除自己）
     * 預期：名稱相同但 ID 也相同，應視為「未重複」，回傳 false
     */
    @Test
    public void testExistsByCategoryNameAndIdNot_ExcludeSelf_ReturnsFalse() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("唯一類別名");
        categoryRepo.save(cat);

        em.flush();

        assertThat(categoryRepo.existsByCategoryNameAndIdNot("唯一類別名", cat.getId()))
                .isFalse();

        System.out.println("✅ testExistsByCategoryNameAndIdNot_ExcludeSelf_ReturnsFalse 通過");
    }

    /**
     * 測試：檢查名稱是否重複（排除他人）
     * 預期：名稱與「他人」重複，應回傳 true
     */
    @Test
    public void testExistsByCategoryNameAndIdNot_ExcludeOthers_ReturnsTrue() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("重複類別名");
        categoryRepo.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("另一類別");
        categoryRepo.save(cat2);

        em.flush();

        assertThat(categoryRepo.existsByCategoryNameAndIdNot("重複類別名", cat2.getId()))
                .isTrue();

        System.out.println("✅ testExistsByCategoryNameAndIdNot_ExcludeOthers_ReturnsTrue 通過");
    }
}