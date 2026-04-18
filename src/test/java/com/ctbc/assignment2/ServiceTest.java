package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.exception.CategoryHierarchyException;
import com.ctbc.assignment2.exception.CategoryNotEmptyException;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest: 整合測試註解，會啟動完整的 Spring 應用程式上下文(Context)，包含了所有的 Bean (Service, Repository 等)
@SpringBootTest
public class ServiceTest {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    // ════════════════════════════════════════════════════
    //   基本 CRUD
    // ════════════════════════════════════════════════════

    // @Test: 宣告此方法為獨立的測試案例
    @Test
    public void testSaveAndFindCategory() {
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("Service測試類別");
        CourseCategoryBean saved = categoryService.save(cat);

        CourseCategoryBean found = categoryService.findById(saved.getId());
        assertThat(found.getCategoryName()).isEqualTo("Service測試類別");
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

        assertThat(courseService.findAll().size()).isGreaterThanOrEqualTo(2);
        System.out.println("✅ testFindAllCourses 通過");
    }

    @Test
    public void testDeleteCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("待刪除課程");
        course.setPrice(500.0);
        CourseBean saved = courseService.save(course);
        Long savedId = saved.getId();

        courseService.deleteById(savedId);

        assertThrows(ResourceNotFoundException.class, () -> courseService.findById(savedId));
        System.out.println("✅ testDeleteCourse 通過");
    }

    @Test
    public void testFindByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> courseService.findById(99999L));
        System.out.println("✅ testFindByIdNotFound 通過");
    }

    @Test
    public void testDeleteNonExistentCourse() {
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteById(99999L));
        System.out.println("✅ testDeleteNonExistentCourse 通過");
    }

    @Test
    public void testDeleteNonExistentCategory() {
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteById(99999L));
        System.out.println("✅ testDeleteNonExistentCategory 通過");
    }

    @Test
    public void testUpdateCourse() {
        CourseBean course = new CourseBean();
        course.setCourseName("原始名稱");
        course.setPrice(100.0);
        CourseBean saved = courseService.save(course);
        Long savedId = saved.getId();

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
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99999L));
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
    //   重複名稱檢查（Service 層）
    // ════════════════════════════════════════════════════

    @Test
    public void testDuplicateCourseNameThrows() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("重複課程_Dup");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("重複課程_Dup");
        c2.setPrice(200.0);
        assertThrows(DuplicateCourseNameException.class, () -> courseService.save(c2));
        System.out.println("✅ testDuplicateCourseNameThrows 通過");
    }

    @Test
    public void testDuplicateCourseNameIgnoreWhitespaceAndCase() {
        String baseName = "Java基礎Test" + System.nanoTime();

        CourseBean c1 = new CourseBean();
        c1.setCourseName(baseName);
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName(baseName.toUpperCase().replace("", " ").trim());
        c2.setPrice(200.0);

        assertThrows(DuplicateCourseNameException.class, () -> courseService.save(c2));
        System.out.println("✅ testDuplicateCourseNameIgnoreWhitespaceAndCase 通過");
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
    public void testUpdateCourseDuplicateNameThrows() {
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
    public void testUpdateCategoryDuplicateNameThrows() {
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

    @Test
    public void testUpdateCourseStatus() {
        CourseBean course = new CourseBean();
        course.setCourseName("狀態更新測試");
        course.setPrice(100.0);
        CourseBean saved = courseService.save(course);

        CourseBean updated = courseService.updateStatus(saved.getId(), CourseStatus.PUBLISHED);
        assertThat(updated.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        System.out.println("✅ testUpdateCourseStatus 通過");
    }

    @Test
    public void testFindPublishedPageOnlyReturnsPublished() {
        CourseBean draft = new CourseBean();
        draft.setCourseName("Draft course");
        draft.setPrice(100.0);
        draft.setStatus(CourseStatus.DRAFT);
        courseService.save(draft);

        CourseBean published = new CourseBean();
        published.setCourseName("Published course");
        published.setPrice(200.0);
        published.setStatus(CourseStatus.PUBLISHED);
        courseService.save(published);

        Page<CourseBean> page = courseService.findPublishedPage(PageRequest.of(0, 10));
        assertThat(page.getContent().stream().allMatch(c -> c.getStatus() == CourseStatus.PUBLISHED)).isTrue();
        System.out.println("✅ testFindPublishedPageOnlyReturnsPublished 通過");
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
    public void testSaveCategoryWithEmptyNameTriggersJpaValidation() {
        // 【修正】JPA persist 時 @NotBlank 仍會觸發，應預期 ConstraintViolationException
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("  ");

        assertThrows(ConstraintViolationException.class, () -> categoryService.save(cat));
        System.out.println("✅ testSaveCategoryWithEmptyName_JPA_Validation觸發 通過");
    }

    @Test
    public void testSaveCourseWithNegativePriceTriggersJpaValidation() {
        // 【修正】JPA persist 時 @PositiveOrZero 仍會觸發，應預期 ConstraintViolationException
        CourseBean course = new CourseBean();
        course.setCourseName("負價格課程_Bypass");
        course.setPrice(-100.0);

        assertThrows(ConstraintViolationException.class, () -> courseService.save(course));
        System.out.println("✅ testSaveCourseWithNegativePrice_JPA_Validation觸發 通過");
    }

    @Test
    public void testSaveCourseWithZeroPriceValidBoundary() {
        // price = 0 符合 @PositiveOrZero，應成功存入
        CourseBean course = new CourseBean();
        course.setCourseName("零元課程_Zero");
        course.setPrice(0.0);
        CourseBean saved = courseService.save(course);

        assertThat(courseService.findById(saved.getId()).getPrice()).isEqualTo(0.0);
        System.out.println("✅ testSaveCourseWithZeroPrice_合法邊界值 通過");
    }

    @Test
    public void testSaveCourseWithNullCategory() {
        CourseBean course = new CourseBean();
        course.setCourseName("無類別Service課程");
        course.setPrice(200.0);
        CourseBean saved = courseService.save(course);

        CourseBean found = courseService.findById(saved.getId());
        assertThat(found.getCategory()).isNull();
        System.out.println("✅ testSaveCourse_無類別_categoryNull 通過");
    }

    @Test
    public void testFindPageCourses() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("分頁測試課程1");
        c1.setPrice(100.0);
        courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("分頁測試課程2");
        c2.setPrice(200.0);
        courseService.save(c2);

        CourseBean c3 = new CourseBean();
        c3.setCourseName("分頁測試課程3");
        c3.setPrice(300.0);
        courseService.save(c3);

        Page<CourseBean> page = courseService.findPage(PageRequest.of(0, 2));
        assertThat(page.getContent().size()).isEqualTo(2);
        System.out.println("✅ testFindPageCourses 通過");
    }

    @Test
    public void testDeleteBatchCourses() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("批次刪除課程1");
        c1.setPrice(100.0);
        CourseBean saved1 = courseService.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("批次刪除課程2");
        c2.setPrice(200.0);
        CourseBean saved2 = courseService.save(c2);

        courseService.deleteBatch(List.of(saved1.getId(), saved2.getId()));

        assertThrows(ResourceNotFoundException.class, () -> courseService.findById(saved1.getId()));
        assertThrows(ResourceNotFoundException.class, () -> courseService.findById(saved2.getId()));
        System.out.println("✅ testDeleteBatchCourses 通過");
    }

    @Test
    public void testSaveBatchCourses_AllOrNothing() {
        CourseBean existing = new CourseBean();
        existing.setCourseName("批次新增重複課程");
        existing.setPrice(999.0);
        courseService.save(existing);

        int beforeCount = courseService.findAll().size();

        CourseBean c1 = new CourseBean();
        c1.setCourseName("批次新增課程1");
        c1.setPrice(100.0);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("批次新增重複課程");
        c2.setPrice(200.0);

        assertThrows(DuplicateCourseNameException.class,
                () -> courseService.saveBatch(List.of(c1, c2)));

        int afterCount = courseService.findAll().size();
        assertThat(afterCount).isEqualTo(beforeCount);
        System.out.println("✅ testSaveBatchCourses_AllOrNothing 通過");
    }

    @Test
    public void testCreateChildCategoryUnderTopLevel() {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setCategoryName("ParentCat" + System.nanoTime());
        CourseCategoryBean savedParent = categoryService.save(parent);

        CourseCategoryBean child = new CourseCategoryBean();
        child.setCategoryName("ChildCat" + System.nanoTime());
        child.setParent(savedParent);

        CourseCategoryBean savedChild = categoryService.save(child);

        assertThat(savedChild.getParent()).isNotNull();
        assertThat(savedChild.getParent().getId()).isEqualTo(savedParent.getId());
        System.out.println("✅ testCreateChildCategoryUnderTopLevel 通過");
    }

    @Test
    public void testRejectChildAsParent() {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setCategoryName("TopParent" + System.nanoTime());
        CourseCategoryBean savedParent = categoryService.save(parent);

        CourseCategoryBean child = new CourseCategoryBean();
        child.setCategoryName("Child" + System.nanoTime());
        child.setParent(savedParent);
        CourseCategoryBean savedChild = categoryService.save(child);

        CourseCategoryBean grandChild = new CourseCategoryBean();
        grandChild.setCategoryName("GrandChild" + System.nanoTime());
        grandChild.setParent(savedChild);

        assertThrows(CategoryHierarchyException.class, () -> categoryService.save(grandChild));
        System.out.println("✅ testRejectChildAsParent 通過");
    }

    @Test
    public void testRejectParentChangeWhenHasChildren() {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setCategoryName("RootParent" + System.nanoTime());
        CourseCategoryBean savedParent = categoryService.save(parent);

        CourseCategoryBean child = new CourseCategoryBean();
        child.setCategoryName("RootChild" + System.nanoTime());
        child.setParent(savedParent);
        categoryService.save(child);

        CourseCategoryBean newParent = new CourseCategoryBean();
        newParent.setCategoryName("NewParent" + System.nanoTime());
        CourseCategoryBean savedNewParent = categoryService.save(newParent);

        savedParent.setParent(savedNewParent);

        assertThrows(CategoryHierarchyException.class, () -> categoryService.save(savedParent));
        System.out.println("✅ testRejectParentChangeWhenHasChildren 通過");
    }

    @Test
    public void testDeleteCategoryWithChildrenNotAllowed() {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setCategoryName("ParentDelete" + System.nanoTime());
        CourseCategoryBean savedParent = categoryService.save(parent);

        CourseCategoryBean child = new CourseCategoryBean();
        child.setCategoryName("ChildDelete" + System.nanoTime());
        child.setParent(savedParent);
        categoryService.save(child);

        assertThrows(CategoryNotEmptyException.class, () -> categoryService.deleteById(savedParent.getId()));
        System.out.println("✅ testDeleteCategoryWithChildrenNotAllowed 通過");
    }

    @Test
    public void testDeleteCategoryWithCoursesNotAllowed() {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setCategoryName("CategoryWithCourse" + System.nanoTime());
        CourseCategoryBean savedCategory = categoryService.save(category);

        CourseBean course = new CourseBean();
        course.setCourseName("CourseWithCategory" + System.nanoTime());
        course.setPrice(100.0);
        course.setCategory(savedCategory);
        courseService.save(course);

        assertThrows(CategoryNotEmptyException.class, () -> categoryService.deleteById(savedCategory.getId()));
        System.out.println("✅ testDeleteCategoryWithCoursesNotAllowed 通過");
    }
}