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
 * 確保我們有理解 Hibernate / JPA 刪除關聯時機的測試類別。
 * 重點驗證：「Set Null」解除關聯策略，以及「Cascade All」串聯刪除策略。
 */

/**
 * 驗證 Hibernate / JPA 在「SET NULL 解除關聯策略」下的刪除行為
 */
@DataJpaTest
public class DeleteStrategyTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // =====================================================
    // Strategy：SET NULL（解除關聯後再刪除分類）
    // =====================================================

    /**
     * 測試情境：
     *   - 有一門課程隸屬某個分類
     *   - 在刪除分類前，先把課程的 category 設為 null
     *
     * 預期結果：
     *   分類成功刪除
     *   課程仍存在
     *   課程的 category 變成 null
     */
    @Test
    public void testDeleteCategory_SetNull() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("待刪除類別");
        categoryRepo.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("孤兒課程");
        course.setPrice(100.0);
        course.setCategory(cat);
        courseRepo.save(course);

        em.flush();
        em.clear();

        // 關鍵步驟：解除關聯（SET NULL）
        CourseBean found = courseRepo.findById(course.getId()).get();
        found.setCategory(null);
        courseRepo.save(found);

        em.flush();
        em.clear();

        // 再刪除分類
        categoryRepo.deleteById(cat.getId());

        em.flush();
        em.clear();

        // 驗證：課程還在，category 為 null
        CourseBean result = courseRepo.findById(course.getId()).get();
        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isNull();

        System.out.println("✅ testDeleteCategory_SetNull 通過");
    }

    /**
     * 測試情境：
     *   - 同一個分類底下有多門課程
     *   - 逐一解除所有課程與分類的關聯
     *
     * 預期結果：
     *   分類刪除成功
     *   所有課程都還在
     *   每門課的 category 都是 null
     */
    @Test
    public void testDeleteCategory_SetNull_ForMultipleCourses() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("多筆待解除類別");
        categoryRepo.save(cat);

        CourseBean c1 = new CourseBean();
        c1.setCourseName("課程 SetNull1");
        c1.setPrice(100.0);
        c1.setCategory(cat);
        courseRepo.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("課程 SetNull2");
        c2.setPrice(200.0);
        c2.setCategory(cat);
        courseRepo.save(c2);

        em.flush();
        em.clear();

        // 將所有屬於該分類的課程解除關聯
        courseRepo.findAll().stream()
                .filter(c -> c.getCategory() != null
                        && c.getCategory().getId().equals(cat.getId()))
                .forEach(c -> {
                    c.setCategory(null);
                    courseRepo.save(c);
                });

        em.flush();
        em.clear();

        // 刪除分類
        categoryRepo.deleteById(cat.getId());

        em.flush();
        em.clear();

        // 驗證：分類消失
        assertThat(categoryRepo.findById(cat.getId())).isEmpty();

        // 驗證：課程存在且 category 為 null
        assertThat(courseRepo.findById(c1.getId()).get().getCategory()).isNull();
        assertThat(courseRepo.findById(c2.getId()).get().getCategory()).isNull();

        System.out.println("✅ testDeleteCategory_SetNull_多筆課程 通過");
    }

    // =====================================================
    // 關聯正向性測試
    // =====================================================

    /**
     * 測試情境：
     *   - 刪除單一課程
     *
     * 預期結果：
     *   課程被刪除
     *   分類不受影響
     */
    @Test
    public void testDeleteCourse_ShouldNotAffectClass() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("不受影響類別");
        categoryRepo.save(cat);

        CourseBean c = new CourseBean();
        c.setCourseName("被刪除的課程");
        c.setPrice(100.0);
        c.setCategory(cat);
        courseRepo.save(c);

        em.flush();
        em.clear();

        // 刪除課程
        courseRepo.deleteById(c.getId());

        em.flush();
        em.clear();

        // 課程消失
        assertThat(courseRepo.findById(c.getId())).isEmpty();

        // 分類仍然存在
        assertThat(categoryRepo.findById(cat.getId())).isPresent();

        System.out.println("✅ testDeleteCourse_不影響類別 通過");
    }
}