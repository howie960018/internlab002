package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * 課程資料 (Course) 之 Repository 與 Entity 行為測試
 * @DataJpaTest : 僅啟動 JPA 相關元件，並在測試結束後自動回滾
 */
@DataJpaTest
public class CourseBeanTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // ============================================================
    // 1. 基本儲存與關聯行為測試
    // ============================================================

    @Test
    public void testSaveCourseWithCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("測試類別");
        categoryRepo.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("測試課程");
        course.setPrice(999.0);
        course.setCategory(cat);
        courseRepo.save(course);

        em.flush();
        em.clear();

        assertThat(courseRepo.findAll()).hasSize(1);
        assertThat(courseRepo.findAll().get(0).getCourseName()).isEqualTo("測試課程");
        System.out.println("✅ testSaveCourseWithCategory 通過");
    }

    @Test
    public void testSaveCourseWithoutCategory() {
        CourseBean course = new CourseBean();
        course.setCourseName("無類別課程");
        course.setPrice(500.0);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        assertThat(found.getCategory()).isNull();
        System.out.println("✅ testSaveCourseWithoutCategory 通過");
    }

    // ============================================================
    // 2. ID 生成與刪除行為測試
    // ============================================================

    @Test
    public void testIdGeneration() {
        CourseBean course = new CourseBean();
        course.setCourseName("ID生成測試");
        course.setPrice(200.0);

        assertThat(course.getId()).isNull();

        courseRepo.save(course);
        em.flush();

        assertThat(course.getId()).isNotNull();
        System.out.println("Generated ID: " + course.getId());
        System.out.println("✅ testIdGeneration 通過");
    }

    @Test
    public void testDeleteCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("待刪除課程");
        course.setPrice(300.0);
        courseRepo.save(course);

        em.flush();
        em.clear();

        courseRepo.deleteById(course.getId());
        em.flush();

        assertThat(courseRepo.findById(course.getId())).isEmpty();
        System.out.println("✅ testDeleteCourse 通過");
    }

    // ============================================================
    // 3. 生命週期時間戳記 (@PrePersist / @PreUpdate) 測試
    // ============================================================

    @Test
    public void testCreatedAtAndUpdatedAt_ShouldNotNull() {
        CourseBean course = new CourseBean();
        course.setCourseName("建立時間測試");
        course.setPrice(100.0);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        System.out.println("✅ testCreatedAtAndUpdatedAt_ShouldNotNull 通過");
    }

    @Test
    public void testCourseUpdate_ShouldUpdateUpdatedAt() throws InterruptedException {
        CourseBean course = new CourseBean();
        course.setCourseName("原始名稱");
        course.setPrice(100.0);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean persisted = courseRepo.findById(course.getId()).get();
        Date updatedAtBefore = persisted.getUpdatedAt();

        // 停頓 50 毫秒確保時間差異
        Thread.sleep(50);

        persisted.setCourseName("修改後名稱");
        courseRepo.save(persisted);

        em.flush();
        em.clear();

        CourseBean updated = courseRepo.findById(course.getId()).get();
        assertThat(updated.getCourseName()).isEqualTo("修改後名稱");
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt().getTime()).isGreaterThanOrEqualTo(updatedAtBefore.getTime());
        System.out.println("✅ testCourseUpdate_ShouldUpdateUpdatedAt 通過");
    }

    @Test
    public void testCreatedAt_ShouldRemainUnchangedAfterUpdate() throws InterruptedException {
        CourseBean course = new CourseBean();
        course.setCourseName("createdAt不變測試");
        course.setPrice(100.0);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean persisted = courseRepo.findById(course.getId()).get();
        Date createdAtBefore = persisted.getCreatedAt();

        Thread.sleep(50);

        persisted.setCourseName("已修改名稱");
        courseRepo.save(persisted);

        em.flush();
        em.clear();

        CourseBean updated = courseRepo.findById(course.getId()).get();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        System.out.println("✅ testCreatedAt_ShouldRemainUnchangedAfterUpdate 通過");
    }
}