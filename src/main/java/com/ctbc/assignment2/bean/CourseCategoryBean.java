

/**
 * 課程類別資料模型 (Entity)
 * 代表資料庫中的課程類別表格，主要用來分類不同的課程。
 */
package com.ctbc.assignment2.bean;

import java.util.List;
import java.util.UUID;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 課程類別資料模型 (Entity)
 * 代表資料庫中的課程類別表格，主要用來分類不同的課程。
 */
@Entity
@Table(name = "course_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseCategoryBean {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "類別名稱不可以為空")
    private String categoryName;

    // 阻斷轉成 JSON 時的無限迴圈（類別找課程、課程又找回類別導致當機）。
    // 同時也避免在查詢單一「類別」時，把底下成千上百堂「課程」全部載入，導致效能變差
    @JsonIgnore
    // 一對多關聯：一個「類別」底下可以有多個「課程」
    @OneToMany(mappedBy = "category")  
    //告訴 JPA：在 course_category 資料表裡，不需要建立任何新欄位來記錄課程
    // 如果想知道這個類別有哪些課程，請去對面 (CourseBean) 找那個叫做 category 的變數，看它的對應關係就好

    //想像 CourseBean 是員工，CourseCategoryBean 是部門。員工身上會掛著寫有部門編號的識別證 (@JoinColumn)；
    // 而部門辦公室裡不需要在牆上刻下所有員工的名字，只需要看誰掛著這個部門的識別證就知道員工是誰了 (mappedBy)。
    private List<CourseBean> courses;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // 自動打卡機：新增時記錄時間
    @PrePersist
    public void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    // 自動打卡機：更新時記錄時間
    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }
}