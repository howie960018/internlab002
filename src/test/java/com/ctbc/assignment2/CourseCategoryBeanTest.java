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
 * 課程分類模組的專屬測試類別
 * @DataJpaTest 保障我們開的是單純用來測試的記憶體資料庫環境
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
     * 測試：是否能單純儲存一個分類
     */
    @Test
    public void testSaveCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Java");
        categoryRepo.save(cat);

        // em.flush() 讓 Hibernate 真正寫進資料庫，em.clear() 讓下次查詢不走記憶體捷徑，確保是最真實的狀況。
        em.flush();
        em.clear();

        // 斷言驗證數量與名稱正不正確
        assertThat(categoryRepo.findAll()).hasSize(1);
        assertThat(categoryRepo.findAll().get(0).getCategoryName()).isEqualTo("Java");
        System.out.println("✅ testSaveCategory 通過");
    }

    /**
     * 測試：把一門課程歸類到某個分類之中，去查資料庫看看這課程到底有沒有成功帶著這個分類回來。
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

        // 從資料庫找回這門課程，取出分類名稱比對看是不是 "Web"
        CourseBean found = courseRepo.findById(course.getId()).get();
        assertThat(found.getCategory().getCategoryName()).isEqualTo("Web");
        System.out.println("✅ testAddCourseToCategory 通過");
    }

    /**
     * 測試：原本課程隸屬於 A 分類，現在我們幫它換到 B 分類，能支援嗎？
     */
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
        course.setCategory(cat1); // 原本在類別1
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        CourseCategoryBean newCat = categoryRepo.findById(cat2.getId()).get();
        
        // 替換分類並儲存！！！
        found.setCategory(newCat);
        courseRepo.save(found);

        em.flush();
        em.clear();

        // 撈出來看斷言是否在 "類別2" 身上了
        CourseBean updated = courseRepo.findById(course.getId()).get();
        assertThat(updated.getCategory().getCategoryName()).isEqualTo("類別2");
        System.out.println("✅ testMoveCourseToAnotherCategory 通過");
    }

    /**
     * 測試：是否可以把課程從分類裡面拿掉 (變成孤兒，也就是無門無派，分類為 null 的裸奔課程)。
     */
    @Test
    public void testRemoveCategoryFromCourse_setNull() {
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
        // 刻意將他的分類拔掉，設為 Null
        found.setCategory(null);
        courseRepo.save(found);

        em.flush();
        em.clear();

        // 確實驗證課程自己變成 Null 分類
        assertThat(courseRepo.findById(course.getId()).get().getCategory()).isNull();
        // 重點：但類別本身還是要活得好好的 (不能因為從課程拿掉，連帶類別也被砍)，所以要檢查類別還存在著。
        assertThat(categoryRepo.findById(cat.getId())).isPresent();
        System.out.println("✅ testRemoveCategoryFromCourse_setNull 通過");
    }

    // ════════════════════════════════════════════════════
    // 剩下的一樣是 Repository 對於 "分類不存在" , "排除自己本身不重複" 的邏輯檢查，同 CourseBeanTest 中的註解精神！
    // ════════════════════════════════════════════════════

    @Test
    public void testCategory_existsByCategoryName_true() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("存在類別名稱");
        categoryRepo.save(cat);
        em.flush();

        assertThat(categoryRepo.existsByCategoryName("存在類別名稱")).isTrue();
        System.out.println("✅ testCategory_existsByCategoryName_true 通過");
    }

    @Test
    public void testCategory_existsByCategoryName_false() {
        assertThat(categoryRepo.existsByCategoryName("完全不存在XYZ")).isFalse();
        System.out.println("✅ testCategory_existsByCategoryName_false 通過");
    }

    @Test
    public void testCategory_existsByCategoryNameAndIdNot_排除自身回false() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("唯一類別名");
        categoryRepo.save(cat);
        em.flush();

        assertThat(categoryRepo.existsByCategoryNameAndIdNot("唯一類別名", cat.getId())).isFalse();
        System.out.println("✅ testCategory_existsByCategoryNameAndIdNot_排除自身回false 通過");
    }

    @Test
    public void testCategory_existsByCategoryNameAndIdNot_排除他人回true() {
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