package com.ctbc.assignment2.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

/**
 * 課程類別資料模型 (Entity)
 * 代表資料庫中的課程類別表格，主要用來分類不同的課程。
 */
@Entity
@Table(name = "course_category")
public class CourseCategoryBean {

    // 類別編號 (主鍵，由資料庫自動產生UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 類別名稱，不可為空白
    @NotBlank(message = "類別名稱不可為空")
    private String categoryName;

    /**
     * 關聯這類別下的所有課程 (一對多關係，一個類別底下可能有多個課程)
     * @JsonIgnore 避免將物件轉換為 JSON 的時候發生無限迴圈 (課程互包分類又包課程)
     * cascade = CascadeType.ALL 代表若此類別被刪除，底下相關的變更操作也會一併刪掉
     * orphanRemoval = true 代表移除與分類斷開連結的孤兒子實體
     */
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseBean> courses;

    // ================= 以下為 Getter / Setter (讓其他程式可以存取或修改這些私有屬性) =================
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<CourseBean> getCourses() { return courses; }
    public void setCourses(List<CourseBean> courses) { this.courses = courses; }
}
