package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionSystemException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 專門負責測試 Service 層 (業務邏輯層) 的測試類別。
 * 【初學者觀念】：
 *   這裡我們使用 @SpringBootTest 來載入完整的 Spring Application Context (包含 DB 連線與設定)，
 *   因為我們想確實測試 Service 連動 Repository 寫入資料庫的整體真實流程。
 *   和 @DataJpaTest 的差異在於，它不只載入資料庫的設定，連 Service 甚至 Controller 都會載入進來。
 */
@SpringBootTest
public class ServiceTest {

    // 直接請 Spring 注射真實的 Service 實作類別進來 (不是假人 Mock)
    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    // ════════════════════════════════════════════════════
    //   基本 CRUD 測試 (建立、讀取、更新、刪除)
    // ════════════════════════════════════════════════════

    @Test
    public void testSaveAndFindCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Service測試類別");
        CourseCategoryBean saved = categoryService.save(cat);

        // 呼叫 Service 的 findById 把剛剛存進去的資料撈出來
        CourseCategoryBean found = categoryService.findById(saved.getId());
        assertThat(found.getCategoryName()).isEqualTo("Service測試類別"); // 確認拿出來的內容是不是我們期望的
        System.out.println("✅ testSaveAndFindCategory 通過");
    }

    @Test
    public void testSaveAndFindCourse() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Service課程類別");
        categoryService.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("Service測試課程");
        course.setPrice(888.0);
        course.setCategory(cat);
        CourseBean saved = courseService.save(course);

        CourseBean found = courseService.findById(saved.getId());
        assertThat(found.getCourseName()).isEqualTo("Service測試課程");
        assertThat(found.getPrice()).isEqualTo(888.0);
        System.out.println("✅ testSaveAndFindCourse 通過");
    }

    @Test
    public void testFindAllCourses() {
        CourseBean course1 = new CourseBean();
        course1.setCourseName("課程A");
        course1.setPrice(100.0);
        courseService.save(course1);

        CourseBean course2 = new CourseBean();
        course2.setCourseName("課程B");
        course2.setPrice(200.0);
        courseService.save(course2);

        // 至少會查到我們剛剛塞的這 2 筆
        assertThat(courseService.findAll().size()).isGreaterThanOrEqualTo(2);
        System.out.println("✅ testFindAllCourses 通過");
    }

    @Test
    public void testDeleteCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("待刪除課程");
        course.setPrice(500.0);
        CourseBean saved = courseService.save(course);
        java.util.UUID savedId = saved.getId();

        courseService.deleteById(savedId);

        // 斷言驗證(assertThrows)：當我們去查一個已經被刪掉的 ID 時，Service 必須要拋出 ResourceNotFoundException！否則測試無法通過。
        assertThrows(ResourceNotFoundException.class, () -> courseService.findById(savedId));
        System.out.println("✅ testDeleteCourse 通過");
    }

    @Test
    public void testFindByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> courseService.findById(java.util.UUID.fromString("00000000-0000-0000-0000-000000009999")));
        System.out.println("✅ testFindByIdNotFound 通過");
    }

    @Test
    public void testDeleteNonExistentCourse() {
        // 刪除一個不存在的 ID 不應該使得系統當機崩潰，所以要使用 assertDoesNotThrow 保證無事發生
        assertDoesNotThrow(() -> courseService.deleteById(java.util.UUID.fromString("00000000-0000-0000-0000-000000009999")));
        System.out.println("✅ testDeleteNonExistentCourse 通過");
    }

    @Test
    public void testDeleteNonExistentCategory() {
        assertDoesNotThrow(() -> categoryService.deleteById(java.util.UUID.fromString("00000000-0000-0000-0000-000000009999")));
        System.out.println("✅ testDeleteNonExistentCategory 通過");
    }

    @Test
    public void testUpdateCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("原始名稱");
        course.setPrice(100.0);
        CourseBean saved = courseService.save(course);
        java.util.UUID savedId = saved.getId();

        // 把撈出來的物件修改內容後再存進去一次 (Spring Data JPA 會自動判斷這是 Update)
        saved.setCourseName("修改後名稱");
        saved.setPrice(999.0);
        courseService.save(saved);

        CourseBean updated = courseService.findById(savedId);
        assertThat(updated.getCourseName()).isEqualTo("修改後名稱");
        assertThat(updated.getPrice()).isEqualTo(999.0);
        System.out.println("✅ testUpdateCourse 通過");
    }

    @Test
    public void testFindCategoryByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(java.util.UUID.fromString("00000000-0000-0000-0000-000000009999")));
        System.out.println("✅ testFindCategoryByIdNotFound 通過");
    }

    @Test
    public void testFindAllCategories() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("列表測試類別");
        categoryService.save(cat);

        assertThat(categoryService.findAll().size()).isGreaterThanOrEqualTo(1);
        System.out.println("✅ testFindAllCategories 通過");
    }

    // ════════════════════════════════════════════════════
    //   重複名稱檢查（Service 層防呆機制測試）
    // ════════════════════════════════════════════════════

    @Test
    public void testDuplicateCourseNameThrows() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("重複課程_Dup");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        // 設定和上面一模一樣的名稱
        c2.setCourseName("重複課程_Dup");
        c2.setPrice(200.0);
        // 當儲存第二筆時，Service 應該要幫我們擋下來拋出我們自訂的例外
        assertThrows(DuplicateCourseNameException.class, () -> courseService.save(c2));
        System.out.println("✅ testDuplicateCourseNameThrows 通過");
    }

    @Test
    public void testDuplicateCategoryNameThrows() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("重複類別_Dup");
        categoryService.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("重複類別_Dup");
        assertThrows(DuplicateCourseNameException.class, () -> categoryService.save(cat2));
        System.out.println("✅ testDuplicateCategoryNameThrows 通過");
    }

    @Test
    public void testUpdateCourseWithSameName_NoException() {
        CourseBean c = new CourseBean();
        c.setCourseName("同名更新課程_SelfUpdate");
        c.setPrice(100.0);
        CourseBean saved = courseService.save(c);

        // 如果我只是更新價錢，並沒有亂改名稱，這種「跟自己同名」不應該被當作重複名稱擋下來！
        saved.setPrice(300.0);
        assertDoesNotThrow(() -> courseService.save(saved));
        System.out.println("✅ testUpdateCourseWithSameName_NoException 通過");
    }

    @Test
    public void testUpdateCategoryName() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("原始類別名_Update");
        CourseCategoryBean saved = categoryService.save(cat);

        saved.setCategoryName("更新後類別名_Update");
        categoryService.save(saved);

        CourseCategoryBean updated = categoryService.findById(saved.getId());
        assertThat(updated.getCategoryName()).isEqualTo("更新後類別名_Update");
        System.out.println("✅ testUpdateCategoryName 通過");
    }

    @Test
    public void testUpdateCategoryWithSameName_NoException() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("類別自身更新_SelfUpdate");
        CourseCategoryBean saved = categoryService.save(cat);

        assertDoesNotThrow(() -> categoryService.save(saved));
        System.out.println("✅ testUpdateCategoryWithSameName_NoException 通過");
    }

    @Test
    public void testUpdateCourse_重複名稱拋例外() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("課程名稱_已存在_Upd");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("課程名稱_要更新_Upd");
        c2.setPrice(200.0);
        CourseBean saved2 = courseService.save(c2);

        saved2.setCourseName("課程名稱_已存在_Upd");
        assertThrows(DuplicateCourseNameException.class, () -> courseService.save(saved2));
        System.out.println("✅ testUpdateCourse_重複名稱拋例外 通過");
    }

    @Test
    public void testUpdateCategory_重複名稱拋例外() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("類別已存在_Upd");
        categoryService.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("類別要更新_Upd");
        CourseCategoryBean saved2 = categoryService.save(cat2);

        saved2.setCategoryName("類別已存在_Upd");
        assertThrows(DuplicateCourseNameException.class, () -> categoryService.save(saved2));
        System.out.println("✅ testUpdateCategory_重複名稱拋例外 通過");
    }

    // ════════════════════════════════════════════════════
    //   邊界值
    //
    //   【修正說明】
    //   Spring Boot 預設會在 JPA persist/update 時執行 Bean Validation（javax/jakarta 整合）。
    //   因此即使繞過 Controller，@NotBlank / @PositiveOrZero 也會在 persist 時觸發。
    //   原本預期「Service 層可存入空白/負數」是錯的：
    //     - 空白 categoryName → ConstraintViolationException
    //     - 負數 price        → ConstraintViolationException
    //
    //   修正：這兩個測試改為「驗証 JPA Bean Validation 確實有在 persist 時運作」。
    //   若要真正繞過，需在 application.properties 加入：
    //     spring.jpa.properties.javax.persistence.validation.mode=none
    //   但這會影響整體行為，不建議。
    // ════════════════════════════════════════════════════

    @Test
    public void testSaveCategoryWithEmptyName_JPA_Validation觸發() {
        // 【修正】JPA persist 時 @NotBlank 仍會觸發，應預期 ConstraintViolationException
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("  ");

        TransactionSystemException ex = assertThrows(
                TransactionSystemException.class,
                () -> categoryService.save(cat)
        );
        assertThat(ex.getRootCause()).isInstanceOf(ConstraintViolationException.class);
        System.out.println("✅ testSaveCategoryWithEmptyName_JPA_Validation觸發 通過");
    }

    @Test
    public void testSaveCourseWithNegativePrice_JPA_Validation觸發() {
        // 【修正】JPA persist 時 @PositiveOrZero 仍會觸發，應預期 ConstraintViolationException
        CourseBean course = new CourseBean();
        course.setCourseName("負價格課程_Bypass");
        course.setPrice(-100.0);

        TransactionSystemException ex = assertThrows(
                TransactionSystemException.class,
                () -> courseService.save(course)
        );
        assertThat(ex.getRootCause()).isInstanceOf(ConstraintViolationException.class);
        System.out.println("✅ testSaveCourseWithNegativePrice_JPA_Validation觸發 通過");
    }

    @Test
    public void testSaveCourseWithZeroPrice_合法邊界值() {
        // price = 0 符合 @PositiveOrZero，應成功存入
        CourseBean course = new CourseBean();
        course.setCourseName("零元課程_Zero");
        course.setPrice(0.0);
        CourseBean saved = courseService.save(course);

        assertThat(courseService.findById(saved.getId()).getPrice()).isEqualTo(0.0);
        System.out.println("✅ testSaveCourseWithZeroPrice_合法邊界值 通過");
    }

    @Test
    public void testSaveCourse_無類別_categoryNull() {
        CourseBean course = new CourseBean();
        course.setCourseName("無類別Service課程");
        course.setPrice(200.0);
        CourseBean saved = courseService.save(course);

        CourseBean found = courseService.findById(saved.getId());
        assertThat(found.getCategory()).isNull();
        System.out.println("✅ testSaveCourse_無類別_categoryNull 通過");
    }
}