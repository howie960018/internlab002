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
@DataJpaTest
public class DeleteStrategyTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // ════════════════════════════════════════════════════
    //   Strategy 1：SET NULL（先解除關聯再刪類別）
    // ════════════════════════════════════════════════════

    /**
     * 測試：當我不希望因為分類被刪掉，底下課程就陪葬，我該怎麼做？
     * 解法：我們得在刪掉分類【之前】，把課程身上的分類「設為 null」(解偶)。
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

        // 關鍵步驟：先把這門課的 category 設為 null（解除關聯）並存檔
        CourseBean found = courseRepo.findById(course.getId()).get();
        found.setCategory(null);
        courseRepo.save(found);
        em.flush();
        em.clear();

        // 然後再安全地去刪除分類
        categoryRepo.deleteById(cat.getId());
        em.flush();
        em.clear();

        // 到結果檢查：課程應仍然健在沒死，只不過 category 為 null
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

        // 利用迴圈解題，一次把所有隸屬這個母分類底下的課程的關聯全都設成 null 解除
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
    //   Strategy 2：CASCADE ALL + orphanRemoval（直接刪類別帶走課程）
    // ════════════════════════════════════════════════════

    /**
     * 測試：我在 CourseCategoryBean 實體類寫了 cascade = CascadeType.ALL 和 orphanRemoval = true。
     * 直接殺掉此分類，底下的課程會跟著被火燒掉嗎？
     */
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

        // 透過 em 重新載入剛才建的分類實體 (必須要先讓它處於受管理的狀態，才能觸發串接刪除)，然後殺了它！
        CourseCategoryBean catToDelete = em.find(CourseCategoryBean.class, cat.getId());
        categoryRepo.delete(catToDelete);
        em.flush();
        em.clear();

        // 期望結果：少掉的課程數量剛好就是那兩堂被帶走的課程
        long afterCount = courseRepo.count();
        System.out.println("刪除後課程總數：" + afterCount);

        assertThat(afterCount).isEqualTo(beforeCount - 2);
        assertThat(courseRepo.findById(course1.getId())).isEmpty();
        assertThat(courseRepo.findById(course2.getId())).isEmpty();
        System.out.println("✅ testDeleteCategory_Cascade 通過");
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

        // 第一道關卡：類別本身要被成功刪除
        assertThat(categoryRepo.findById(cat.getId())).isEmpty();
        // 第二道關卡：底下的課程要受 Cascade 作用被牽連帶走
        assertThat(courseRepo.findById(c.getId())).isEmpty();
        System.out.println("✅ testDeleteCategory_Cascade_類別也消失 通過");
    }

    /**
     * 相反的測試：砍單一課程的話可以影響母類別嗎？
     * 答案是：絕不行！（多對一的設計，只允許一(母)牽連多(子)）
     */
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

        // 砍課程
        courseRepo.deleteById(c.getId());
        em.flush();
        em.clear();

        assertThat(courseRepo.findById(c.getId())).isEmpty();
        // 斷言：母分類(category)老神在在不受影響
        assertThat(categoryRepo.findById(cat.getId())).isPresent();
        System.out.println("✅ testDeleteCourse_不影響類別 通過");
    }
}