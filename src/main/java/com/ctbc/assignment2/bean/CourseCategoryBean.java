package com.ctbc.assignment2.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 課程類別實體 (Entity)
 * 這代表資料庫中的 "course_category" 資料表。
 */
@Entity
@Table(name = "course_category")
public class CourseCategoryBean {

    // @Id: 標記此為資料表主鍵 (Primary Key)
    // @GeneratedValue: 設定主鍵值由資料庫自動產生 (Auto Increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank: 驗證輸入字串不可為 null、不可為空，且不全為空白字元
    @NotBlank(message = "類別名稱不可為空")
    private String categoryName;

    // @ManyToOne: 自我關聯 (Parent)
    // parent_id 為 null 代表主類別
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CourseCategoryBean parent;

    // @OneToMany: 子類別列表
    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<CourseCategoryBean> children;

    // @JsonIgnore: 防止物件轉換成 JSON 格式時發生無窮迴圈 (雙向關聯常見的問題)
    // @OneToMany: 「一對多」關聯 (一個類別可以有多個課程)。mappedBy = "category" 表示由多方(CourseBean)的 "category" 欄位來維護外鍵關係
    // cascade: 設定聯動操作，例如新增、合併、刷新等會連動影響子實體，但不包含移除 (CascadeType.REMOVE)
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<CourseBean> courses;

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<CourseBean> getCourses() { return courses; }
    public void setCourses(List<CourseBean> courses) { this.courses = courses; }

    public CourseCategoryBean getParent() { return parent; }
    public void setParent(CourseCategoryBean parent) { this.parent = parent; }

    public List<CourseCategoryBean> getChildren() { return children; }
    public void setChildren(List<CourseCategoryBean> children) { this.children = children; }
}
