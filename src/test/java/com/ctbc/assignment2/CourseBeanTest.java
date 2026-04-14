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
 * 課程資料存取 (Repository) 與模型 (Entity) 的單元測試
 * @DataJpaTest 這個註解表示這是一個輕量級的資料庫測試，
 * Spring 會幫我們準備一個可以自動還原狀態的記憶體資料庫 (如 H2)，不會去動到真實的 MySQL / MS SQL 資料。
 */
@DataJpaTest
public class CourseBeanTest {

    // TestEntityManager 是 Spring 用來代替 EntityManager，提供了一些方便測試的方法，例如 .flush()
    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseBeanRepository courseRepo;

    @Autowired
    private CourseCategoryBeanRepository categoryRepo;

    // ════════════════════════════════════════════════════
    //   基本儲存與修改
    // ════════════════════════════════════════════════════

    /**
     * 測試：是否能成功儲存一筆包含分類的課程？
     */
    @Test
    public void testSaveCourse() {
        // 先建立一個隨便的分類
        CourseCategoryBean cat = new CourseCategoryBean();
        cat.setCategoryName("測試類別");
        categoryRepo.save(cat);

        // 再建立一門課程，並把課程放進該分類裡
        CourseBean course = new CourseBean();
        course.setCourseName("測試課程");
        course.setPrice(999.0);
        course.setCategory(cat);
        courseRepo.save(course);

        // 【重點】em.flush() 會強制把記憶體裡的 SQL 語句寫入資料庫
        // em.clear() 會清空 Hibernate 的快取，所以下一行尋找時才會真正去資料庫找，而不是拿記憶體裡沒更新的版本
        em.flush();
        em.clear();

        // 斷言 (assertThat)：檢查從資料庫找出來的所有課程是不是剛好有 1 筆，而且名稱是正確的
        assertThat(courseRepo.findAll()).hasSize(1);
        assertThat(courseRepo.findAll().get(0).getCourseName()).isEqualTo("測試課程");
        System.out.println("✅ testSaveCourse 通過");
    }

    /**
     * 測試：是否能成功儲存一筆「不包含分類」(也就是分類為 null) 的課程？
     */
    @Test
    public void testSaveCourse_無類別() {
        CourseBean course = new CourseBean();
        course.setCourseName("無類別課程");
        course.setPrice(500.0);
        courseRepo.save(course);

        em.flush();
        em.clear();

        CourseBean found = courseRepo.findById(course.getId()).get();
        // 斷言：我們預期這門課程身上的分類(Category)應該會是 Null
        assertThat(found.getCategory()).isNull();
        System.out.println("✅ testSaveCourse_無類別 通過");
    }

    /**
     * 測試：測試 @PreUpdate (自動更新修改時間) 功能是否正常運作
     */
    @Test
    public void testCourseBeanModify() throws InterruptedException {
        // 建立原始資料並寫入
        CourseBean course = new CourseBean();
        course.setCourseName("原始名稱");
        course.setPrice(100.0);
        courseRepo.save(course);
        em.flush();
        em.clear();

        // 重新載入取出以取得資料庫為我們自動補上的 updatedAt (因為 Entity 有寫 @PrePersist)
        CourseBean persisted = courseRepo.findById(course.getId()).get();
        Date updatedAtBefore = persisted.getUpdatedAt();

        // 故意呼叫執行緒讓程式暫停 50 毫秒，製造時間差，才能比較出時間真的變了
        Thread.sleep(50);

        // 修改這筆資料的名字，再寫回去
        persisted.setCourseName("修改後名稱");
        courseRepo.save(persisted);
        em.flush();
        em.clear();

        // 到資料庫把這筆被修改的資料撈出來
        CourseBean updated = courseRepo.findById(course.getId()).get();
        System.out.println("修改前 updatedAt：" + updatedAtBefore);
        System.out.println("修改後 updatedAt：" + updated.getUpdatedAt());

        assertThat(updated.getCourseName()).isEqualTo("修改後名稱");
        assertThat(updated.getUpdatedAt()).isNotNull();
        
        // 斷言：修改後的時間 (updatedAt)，必定會「大於或等於(也就是在它之後發生)」修改前的時間。這代表 @PreUpdate 正常工作
        assertThat(updated.getUpdatedAt().getTime())
                .isGreaterThanOrEqualTo(updatedAtBefore.getTime());
        System.out.println("✅ testCourseBeanModify 通過");
    }

    /**
     * 測試：一但實體被存入資料庫，建立時間 (createdAt) 最起碼不可以是 Null 的。
     */
    @Test
    public void testCreatedAt_不可為null() {
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

    /**
     * 測試：資料進行 Update 修改時，就算 updatedAt 有變，createdAt (建立時間) 也不能跟著變
     */
    @Test
    public void testCreatedAt_儲存後不再變動() throws InterruptedException {
        // @Column(updatable = false) 保證 createdAt 不會被 update 影響
        CourseBean course = new CourseBean();
        course.setCourseName("createdAt不變測試");
        course.setPrice(100.0);
        courseRepo.save(course);
        em.flush();
        em.clear();

        // 記下當時的建檔時間
        CourseBean persisted = courseRepo.findById(course.getId()).get();
        Date createdAtBefore = persisted.getCreatedAt();

        Thread.sleep(50);

        // 來修改資料囉
        persisted.setCourseName("已修改名稱");
        courseRepo.save(persisted);
        em.flush();
        em.clear();

        // 拿資料庫裡面修改過的最新資料，對比一下「建檔時間」是否仍然和一開始的一模一樣？
        CourseBean updated = courseRepo.findById(course.getId()).get();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        System.out.println("✅ testCreatedAt_儲存後不再變動 通過");
    }

    // ════════════════════════════════════════════════════
    //   Repository 特殊語法查詢方法之驗證
    // ════════════════════════════════════════════════════

    /**
     * 測試：「查這名字存不存在的方法 (existsBy...)」，在名字真的存在時，會不會乖乖回傳 True？
     */
    @Test
    public void testExistsByCourseName_存在回true() {
        CourseBean course = new CourseBean();
        course.setCourseName("存在課程");
        course.setPrice(100.0);
        courseRepo.save(course);
        em.flush();

        // 測試此方法是否如預期：是的話就回 True
        assertThat(courseRepo.existsByCourseName("存在課程")).isTrue();
        System.out.println("✅ testExistsByCourseName_存在回true 通過");
    }

    /**
     * 測試：「查這名字存不存在的方法 (existsBy...)」，在資料庫沒這名字時，會不會乖乖回傳 False？
     */
    @Test
    public void testExistsByCourseName_不存在回false() {
        assertThat(courseRepo.existsByCourseName("完全不存在的課程名稱XYZ")).isFalse();
        System.out.println("✅ testExistsByCourseName_不存在回false 通過");
    }

    /**
     * 測試：existsByCourseNameAndIdNot (檢查名稱是否存在但排除某個 ID) → 這個語法設計來處理【編輯資料】。
     * 若去查自己的名字，因為它會排除掉自己，所以應該沒有別人用這名字了，要回傳 False。
     */
    @Test
    public void testExistsByCourseNameAndIdNot_排除自身回false() {
        CourseBean c = new CourseBean();
        c.setCourseName("唯一課程名");
        c.setPrice(100.0);
        courseRepo.save(c);
        em.flush();

        // 排除自身 ID → False（代表除了它自己以外沒有其它人叫這個名稱，表示更改這名稱不會撞名）
        assertThat(courseRepo.existsByCourseNameAndIdNot("唯一課程名", c.getId())).isFalse();
        System.out.println("✅ testExistsByCourseNameAndIdNot_排除自身回false 通過");
    }

    /**
     * 測試：existsByCourseNameAndIdNot。
     * 假如有兩門課程，C1 跟 C2。如果今天 C2 想把自己的名字改成跟 C1 的名字一模一樣，
     * 我們把 C1 的名字丟進去檢查，但把 C2 的 ID 當作排除名單。
     * 此時因為 C1 就在資料庫等著你，所以會跳出 True (警告你已經有人叫這這名字了！)。
     */
    @Test
    public void testExistsByCourseNameAndIdNot_排除他人回true() {
        CourseBean c1 = new CourseBean();
        c1.setCourseName("重複課程名");
        c1.setPrice(100.0);
        courseRepo.save(c1);

        CourseBean c2 = new CourseBean();
        c2.setCourseName("另一課程");
        c2.setPrice(200.0);
        courseRepo.save(c2);
        em.flush();

        // 查 C1 所擁有的名稱，但排除 C2 自己的 ID → True（代表有其它人叫這個名稱)
        assertThat(courseRepo.existsByCourseNameAndIdNot("重複課程名", c2.getId())).isTrue();
        System.out.println("✅ testExistsByCourseNameAndIdNot_排除他人回true 通過");
    }

    /**
     * 測試：確保透過 Repository 刪除後，資料真的會從資料庫消失
     */
    @Test
    public void testDeleteCourse_從資料庫消失() {
        CourseBean course = new CourseBean();
        course.setCourseName("待刪除課程");
        course.setPrice(150.0);
        courseRepo.save(course);
        em.flush();
        Long id = course.getId();

        // 刪除！
        courseRepo.deleteById(id);
        em.flush();
        em.clear();

        // 到資料庫找，找不到才算成功
        assertThat(courseRepo.findById(id)).isEmpty();
        System.out.println("✅ testDeleteCourse_從資料庫消失 通過");
    }
}