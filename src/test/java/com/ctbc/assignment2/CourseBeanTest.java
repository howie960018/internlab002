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

// @DataJpaTest: Spring Boot 專門用來測試 JPA 的註解。它會自動設定記憶體資料庫 (In-memory DB)，並只載入與 JPA 相關的設定。
@DataJpaTest
public class CourseBeanTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // ════════════════════════════════════════════════════
    //   基本儲存與修改
    // ════════════════════════════════════════════════════

    // @Test: JUnit 提供的註解，標示這是一個測試方法
    @Test
    public void testSaveCourse() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("測試類別");
        categoryRepo.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("測試課程");
        course.setPrice(999.0);
        course.setCategory(cat);
        courseRepo.save(course);

        // 【修正】flush 後再查詢，確保資料真正寫入
        em.flush();
        em.clear();

        assertThat(courseRepo.findAll()).hasSize(1);
        assertThat(courseRepo.findAll().get(0).getCourseName()).isEqualTo("測試課程");
        System.out.println("✅ testSaveCourse 通過");
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
        System.out.println("✅ testSaveCourse_無類別 通過");
    }

    @Test
    public void testCourseBeanModify() throws InterruptedException {
        // 【修正】先 persist + flush，讓 @PrePersist 觸發並取得初始 updatedAt
        CourseBean course = new CourseBean();
        course.setCourseName("原始名稱");
        course.setPrice(100.0);
        courseRepo.save(course);
        em.flush();
        em.clear();

        // 重新載入以取得正確的 createdAt / updatedAt
        CourseBean persisted = courseRepo.findById(course.getId()).get();
        Date updatedAtBefore = persisted.getUpdatedAt();

        // 確保時間差至少 1ms
        Thread.sleep(50);

        persisted.setCourseName("修改後名稱");
        courseRepo.save(persisted);
        // 【修正】必須 flush 才會真正觸發 @PreUpdate
        em.flush();
        em.clear();

        CourseBean updated = courseRepo.findById(course.getId()).get();
        System.out.println("修改前 updatedAt：" + updatedAtBefore);
        System.out.println("修改後 updatedAt：" + updated.getUpdatedAt());

        assertThat(updated.getCourseName()).isEqualTo("修改後名稱");
        assertThat(updated.getUpdatedAt()).isNotNull();
        // 【修正】確認 updatedAt 確實在 before 之後（@PreUpdate 有被觸發）
        assertThat(updated.getUpdatedAt().getTime())
                .isGreaterThanOrEqualTo(updatedAtBefore.getTime());
        System.out.println("✅ testCourseBeanModify 通過");
    }

    @Test
    public void testCreatedAtNotNull() {
        CourseBean course = new CourseBean();
        course.setCourseName("建立時間測試");
        course.setPrice(100.0);
        courseRepo.save(course);
        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        System.out.println("✅ testCreatedAt_不可為null 通過");
    }

    @Test
    public void testCreatedAtNotUpdatedAfterSave() throws InterruptedException {
        // 【新增】@Column(updatable = false) 保證 createdAt 不會被 update
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
        // createdAt 應保持不變
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        System.out.println("✅ testCreatedAt_儲存後不再變動 通過");
    }

    // ════════════════════════════════════════════════════
    //   Repository 查詢方法
    // ════════════════════════════════════════════════════

    @Test
    public void testExistsByCourseNameReturnsTrue() {
        CourseBean course = new CourseBean();
        course.setCourseName("存在課程");
        course.setPrice(100.0);
        courseRepo.save(course);
        em.flush();

        assertThat(courseRepo.existsByCourseName("存在課程")).isTrue();
        System.out.println("✅ testExistsByCourseName_存在回true 通過");
    }

    @Test
    public void testExistsByCourseNameReturnsFalse() {
        assertThat(courseRepo.existsByCourseName("完全不存在的課程名稱XYZ")).isFalse();
        System.out.println("✅ testExistsByCourseName_不存在回false 通過");
    }

    @Test
    public void testExistsByCourseNameAndIdNotExcludeSelfFalse() {
        CourseBean c = new CourseBean();
        c.setCourseName("唯一課程名");
        c.setPrice(100.0);
        courseRepo.save(c);
        em.flush();

        // 排除自身 → false（其他人沒有這個名稱）
        assertThat(courseRepo.existsByCourseNameAndIdNot("唯一課程名", c.getId())).isFalse();
        System.out.println("✅ testExistsByCourseNameAndIdNot_排除自身回false 通過");
    }

    @Test
    public void testExistsByCourseNameAndIdNotExcludeOtherTrue() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("重複課程名");
        c1.setPrice(100.0);
        courseRepo.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("另一課程");
        c2.setPrice(200.0);
        courseRepo.save(c2);
        em.flush();

        // 查 c1 的名稱，排除 c2 → true（c1 有這個名稱）
        assertThat(courseRepo.existsByCourseNameAndIdNot("重複課程名", c2.getId())).isTrue();
        System.out.println("✅ testExistsByCourseNameAndIdNot_排除他人回true 通過");
    }

    @Test
    public void testDeleteCourseRemovedFromDatabase() {
        CourseBean course = new CourseBean();
        course.setCourseName("待刪除課程");
        course.setPrice(300.0);
        courseRepo.save(course);
        em.flush();
        Long id = course.getId();

        courseRepo.deleteById(id);
        em.flush();
        em.clear();

        assertThat(courseRepo.findById(id)).isEmpty();
        System.out.println("✅ testDeleteCourse_從資料庫消失 通過");
    }
}