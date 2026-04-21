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


import java.util.List;
import java.util.UUID;


/**
 * Service 層（業務邏輯層）整合測試
 *
 * 使用 @SpringBootTest：
 * - 啟動完整 Spring 容器
 * - 驗證 Service + Repository + Transaction 的整合行為
 * - 使用 UUID 作為主鍵（與實際系統一致）
 */
@SpringBootTest
public class ServiceTest {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    // =====================================================
    // 基本 CRUD 行為
    // =====================================================

    /**
     * 測試：儲存分類後，是否可以正確依 ID 查詢回來
     */
    @Test
    public void testSaveAndFindCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Service測試類別");

        CourseCategoryBean saved = categoryService.save(cat);
        CourseCategoryBean found = categoryService.findById(saved.getId());

        assertThat(found.getCategoryName()).isEqualTo("Service測試類別");
    }

    /**
     * 測試：儲存課程後，是否可以正確依 ID 查詢回來
     */
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
    }

    /**
     * 測試：查詢所有課程
     */
    @Test
    public void testFindAllCourses() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("課程 A");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("課程 B");
        c2.setPrice(200.0);
        courseService.save(c2);

        assertThat(courseService.findAll().size())
                .isGreaterThanOrEqualTo(2);
    }

    /**
     * 測試：刪除存在的課程後，再查詢應拋出 ResourceNotFoundException
     */
    @Test
    public void testDeleteCourse_ShouldRemoveCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("待刪除課程");
        course.setPrice(500.0);

        CourseBean saved = courseService.save(course);
        UUID id = saved.getId();

        courseService.deleteById(id);

        assertThrows(ResourceNotFoundException.class,
                () -> courseService.findById(id));
    }

    /**
     * 測試：查詢不存在的課程 ID，應拋出 ResourceNotFoundException
     */
    @Test
    public void testFindCourseById_NotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.findById(UUID.randomUUID()));
    }

    /**
     * 測試：刪除不存在的課程，應拋出 ResourceNotFoundException
     */
    @Test
    public void testDeleteNonExistentCourse_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.deleteById(UUID.randomUUID()));
    }

    /**
     * 測試：刪除不存在的分類，應拋出 ResourceNotFoundException
     */
    @Test
    public void testDeleteNonExistentCategory_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteById(UUID.randomUUID()));
    }

    /**
     * 測試：更新課程資料是否成功
     */
    @Test
    public void testUpdateCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("原始名稱");
        course.setPrice(100.0);

        CourseBean saved = courseService.save(course);
        UUID id = saved.getId();

        saved.setCourseName("修改後名稱");
        saved.setPrice(999.0);
        courseService.save(saved);

        CourseBean updated = courseService.findById(id);

        assertThat(updated.getCourseName()).isEqualTo("修改後名稱");
        assertThat(updated.getPrice()).isEqualTo(999.0);
    }

    // =====================================================
    // Category / Course 查詢關聯
    // =====================================================

    /**
     * 測試：查詢不存在的分類 ID，應拋出 ResourceNotFoundException
     */
    @Test
    public void testFindCategoryById_NotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.findById(UUID.randomUUID()));
    }

    /**
     * 測試：查詢所有分類
     */
    @Test
    public void testFindAllCategories() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("列表測試類別");
        categoryService.save(cat);

        assertThat(categoryService.findAll().size())
                .isGreaterThanOrEqualTo(1);
    }

    /**
     * 測試：依分類 ID 查詢課程清單
     */
    @Test
    public void testFindCoursesByCategoryId() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("分類 A");
        CourseCategoryBean savedCat = categoryService.save(cat);

        CourseBean c1 = new CourseBean();
        c1.setCourseName("課程 1");
        c1.setPrice(100.0);
        c1.setCategory(savedCat);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("課程 2");
        c2.setPrice(200.0);
        c2.setCategory(savedCat);
        courseService.save(c2);

        List<CourseBean> result =
                courseService.findByCategoryId(savedCat.getId());

        assertThat(result).hasSize(2);
    }

    /**
     * 測試：查詢不存在分類的課程，應拋出 ResourceNotFoundException
     */
    @Test
    public void testFindCoursesByNonExistingCategory_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.findByCategoryId(UUID.randomUUID()));
    }

    // =====================================================
    // 重複名稱檢查（新增 / 更新）
    // =====================================================

    /**
     * 測試：新增課程時，名稱重複應拋出 DuplicateCourseNameException
     */
    @Test
    public void testCreateCourseWithDuplicateName_ShouldThrowException() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("重複課程_Dup");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("重複課程_Dup");
        c2.setPrice(200.0);

        assertThrows(DuplicateCourseNameException.class,
                () -> courseService.save(c2));
    }

    /**
     * 測試：新增分類時，名稱重複應拋出 DuplicateCourseNameException
     */
    @Test
    public void testCreateCategoryWithDuplicateName_ShouldThrowException() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("重複類別_Dup");
        categoryService.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("重複類別_Dup");

        assertThrows(DuplicateCourseNameException.class,
                () -> categoryService.save(cat2));
    }

    /**
     * 測試：更新課程但名稱未變，不應拋出例外
     */
    @Test
    public void testUpdateCourseWithSameName_ShouldNotThrowException() {
        CourseBean course = new CourseBean();
        course.setCourseName("同名更新課程");
        course.setPrice(100.0);

        CourseBean saved = courseService.save(course);
        saved.setPrice(300.0);

        assertDoesNotThrow(() -> courseService.save(saved));
    }

    /**
     * 測試：更新分類名稱是否成功
     */
    @Test
    public void testUpdateCategoryName() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("原始類別名");
        CourseCategoryBean saved = categoryService.save(cat);

        saved.setCategoryName("更新後類別名");
        categoryService.save(saved);

        CourseCategoryBean updated =
                categoryService.findById(saved.getId());

        assertThat(updated.getCategoryName())
                .isEqualTo("更新後類別名");
    }

    /**
     * 測試：更新分類但名稱相同，不應拋出例外
     */
    @Test
    public void testUpdateCategoryWithSameName_ShouldNotThrowException() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("類別自身更新");
        CourseCategoryBean saved = categoryService.save(cat);

        assertDoesNotThrow(() -> categoryService.save(saved));
    }

    /**
     * 測試：更新課程為已存在的名稱，應拋出 DuplicateCourseNameException
     */
    @Test
    public void testUpdateCourseWithDuplicateName_ShouldThrowException() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("已存在名稱");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("要更新名稱");
        c2.setPrice(200.0);
        CourseBean saved2 = courseService.save(c2);

        saved2.setCourseName("已存在名稱");

        assertThrows(DuplicateCourseNameException.class,
                () -> courseService.save(saved2));
    }

    /**
     * 測試：更新分類為已存在名稱，應拋出 DuplicateCourseNameException
     */
    @Test
    public void testUpdateCategoryWithDuplicateName_ShouldThrowException() {
        CourseCategoryBean cat1 = new CourseCategoryBean();
        cat1.setCategoryName("類別已存在");
        categoryService.save(cat1);

        CourseCategoryBean cat2 = new CourseCategoryBean();
        cat2.setCategoryName("類別要更新");
        CourseCategoryBean saved2 = categoryService.save(cat2);

        saved2.setCategoryName("類別已存在");

        assertThrows(DuplicateCourseNameException.class,
                () -> categoryService.save(saved2));
    }

    // =====================================================
    // Bean Validation / 邊界值
    // =====================================================

    /**
     * 測試：分類名稱為空白時，觸發 JPA Validation 失敗
     */
    @Test
    public void testSaveCategoryWithEmptyName_ShouldTriggerValidation() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName(" ");

        assertThrows(
                org.springframework.transaction.TransactionSystemException.class,
                () -> categoryService.save(cat)
        );
    }

    /**
     * 測試：課程價格為負數時，觸發 JPA Validation 失敗
     */
    @Test
    public void testSaveCourseWithNegativePrice_ShouldTriggerValidation() {
        CourseBean course = new CourseBean();
        course.setCourseName("負價格課程");
        course.setPrice(-1.0);

        assertThrows(
                org.springframework.transaction.TransactionSystemException.class,
                () -> courseService.save(course)
        );
    }

    /**
     * 測試：課程價格為 0（合法邊界值）
     */
    @Test
    public void testSaveCourseWithZeroPrice_ShouldBeAllowed() {
        CourseBean course = new CourseBean();
        course.setCourseName("零元課程");
        course.setPrice(0.0);

        CourseBean saved = courseService.save(course);

        assertThat(courseService.findById(saved.getId()).getPrice())
                .isEqualTo(0.0);
    }

    /**
     * 測試：儲存沒有分類的課程（category 為 null）
     */
    @Test
    public void testSaveCourseWithoutCategory_ShouldAllowNull() {
        CourseBean course = new CourseBean();
        course.setCourseName("無類別課程");
        course.setPrice(200.0);

        CourseBean saved = courseService.save(course);
        CourseBean found = courseService.findById(saved.getId());

        assertThat(found.getCategory()).isNull();
    }

    /**
     * 測試：刪除分類時，底下課程仍保留，且其 category 會被設為 null
     */
    @Test
    public void testDeleteCategory_ShouldKeepCoursesAndSetCategoryNull() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("將被刪除的類別");
        CourseCategoryBean savedCat = categoryService.save(cat);

        CourseBean course = new CourseBean();
        course.setCourseName("分類下的課程");
        course.setPrice(123.0);
        course.setCategory(savedCat);
        CourseBean savedCourse = courseService.save(course);

        UUID courseId = savedCourse.getId();
        UUID categoryId = savedCat.getId();

        categoryService.deleteById(categoryId);

        CourseBean foundCourse = courseService.findById(courseId);
        assertNotNull(foundCourse);
        assertNull(foundCourse.getCategory());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.findById(categoryId));
    }
}