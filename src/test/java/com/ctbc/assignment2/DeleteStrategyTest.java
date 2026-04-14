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

// @DataJpaTest: 專心測試資料庫層 (DB)。自動組態 In-memory DB，不載入 Controller 等元件，測試速度快且資料在各個 @Test 結束後會自動 Rollback
@DataJpaTest
public class DeleteStrategyTest {

    @Autowired
    private TestEntityManager em; // TestEntityManager 是一個給測項用的特殊對象，可以用來直接控制 entityManager(快取、flush)

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // ════════════════════════════════════════════════════
    //   Strategy 1：SET NULL（先解除關聯再刪類別）
    // ════════════════════════════════════════════════════

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

        // 先把 category 設為 null（解除關聯）
        CourseBean found = courseRepo.findById(course.getId()).get();
        found.setCategory(null);
        courseRepo.save(found);
        em.flush();
        em.clear();

        // 再刪除類別
        categoryRepo.deleteById(cat.getId());
        em.flush();
        em.clear();

        // 課程應仍然存在，且 category 為 null
        CourseBean result = courseRepo.findById(course.getId()).get();
        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isNull();
        System.out.println("✅ testDeleteCategory_SetNull 通過");
    }

    @Test
    public void testDeleteCategory_SetNull_多筆課程() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("多筆待解除類別");
        categoryRepo.save(cat);

        CourseBean c1 = new CourseBean();
        c1.setCourseName("課程SetNull1");
        c1.setPrice(100.0);
        c1.setCategory(cat);
        courseRepo.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("課程SetNull2");
        c2.setPrice(200.0);
        c2.setCategory(cat);
        courseRepo.save(c2);

        em.flush();
        em.clear();

        // 解除所有關聯
        courseRepo.findAll().stream()
                .filter(c -> c.getCategory() != null && c.getCategory().getId().equals(cat.getId()))
                .forEach(c -> { c.setCategory(null); courseRepo.save(c); });
        em.flush();
        em.clear();

        categoryRepo.deleteById(cat.getId());
        em.flush();
        em.clear();

        assertThat(categoryRepo.findById(cat.getId())).isEmpty();
        // 課程依然存在，且 category 為 null
        assertThat(courseRepo.findById(c1.getId()).get().getCategory()).isNull();
        assertThat(courseRepo.findById(c2.getId()).get().getCategory()).isNull();
        System.out.println("✅ testDeleteCategory_SetNull_多筆課程 通過");
    }

    // ════════════════════════════════════════════════════
    //   Strategy 2：PreRemove set null (自動把類別設為 null，不連帶刪除課程)
    // ════════════════════════════════════════════════════

    @Test
    public void testDeleteCategory_Cascade() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("連帶刪除類別");
        categoryRepo.save(cat);

        CourseBean course1 = new CourseBean();
        course1.setCourseName("課程X");
        course1.setPrice(200.0);
        course1.setCategory(cat);
        courseRepo.save(course1);

        CourseBean course2 = new CourseBean();
        course2.setCourseName("課程Y");
        course2.setPrice(300.0);
        course2.setCategory(cat);
        courseRepo.save(course2);

        em.flush();
        em.clear();

        long beforeCount = courseRepo.count();
        System.out.println("刪除前課程總數：" + beforeCount);

        CourseCategoryBean catToDelete = em.find(CourseCategoryBean.class, cat.getId());
        categoryRepo.delete(catToDelete);
        em.flush();
        em.clear();

        long afterCount = courseRepo.count();
        System.out.println("刪除後課程總數：" + afterCount);

        // 課程不應被刪除
        assertThat(afterCount).isEqualTo(beforeCount);
        
        // 但類別應該變成 null
        assertThat(courseRepo.findById(course1.getId()).get().getCategory()).isNull();
        assertThat(courseRepo.findById(course2.getId()).get().getCategory()).isNull();
        System.out.println("✅ testDeleteCategory_Cascade (現已改為自動 set null) 通過");
    }

    @Test
    public void testDeleteCategory_Cascade_類別也消失() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("連帶刪除類別2");
        categoryRepo.save(cat);

        CourseBean c = new CourseBean();
        c.setCourseName("課程Z");
        c.setPrice(500.0);
        c.setCategory(cat);
        courseRepo.save(c);

        em.flush();
        em.clear();

        CourseCategoryBean catToDelete = em.find(CourseCategoryBean.class, cat.getId());
        categoryRepo.delete(catToDelete);
        em.flush();
        em.clear();

        // 類別本身也應消失
        assertThat(categoryRepo.findById(cat.getId())).isEmpty();
        // 關聯課程不消失，但類別變 null
        assertThat(courseRepo.findById(c.getId())).isPresent();
        assertThat(courseRepo.findById(c.getId()).get().getCategory()).isNull();
        System.out.println("✅ testDeleteCategory_Cascade_類別也消失 (現已改為自動 set null) 通過");
    }

    @Test
    public void testDeleteCourse_不影響類別() {
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

        // 刪課程不應影響類別
        courseRepo.deleteById(c.getId());
        em.flush();
        em.clear();

        assertThat(courseRepo.findById(c.getId())).isEmpty();
        assertThat(categoryRepo.findById(cat.getId())).isPresent();
        System.out.println("✅ testDeleteCourse_不影響類別 通過");
    }
}