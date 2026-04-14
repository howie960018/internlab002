package com.ctbc.assignment2.bean;

import java.util.Date;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 課程資料模型 (Entity)
 * 代表資料庫中的課程表格，存放課程相關的資料欄位。
 */
@Entity
@Table(name = "course")
public class CourseBean {

    // 課程編號 (主鍵，由資料庫自動產生遞增數字)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 課程名稱，不可為空白
    @NotBlank(message = "課程名稱不可為空")
    private String courseName;

    // 課程價格，不可為空且必須是正數或零
    @NotNull(message = "價格不可為空")
    @PositiveOrZero(message = "價格不可為負數")
    private Double price;

    // 課程所屬類別 (多對一關係，一門課程對應一個類別)，這欄位允許為空
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private CourseCategoryBean category;

    // 資料建立時間 (時間戳記，一旦建立後就不可修改)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt;

    // 資料最後更新時間
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    /**
     * 在資料第一次寫入資料庫之前執行的動作。
     * 自動設定建立時間與更新時間。
     */
    @PrePersist
    public void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    /**
     * 在資料更新前執行的動作。
     * 自動更新最後修改時間。
     */
    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }

    // ================= 以下為 Getter / Setter (讓其他程式可以存取或修改這些私有屬性) =================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public CourseCategoryBean getCategory() { return category; }
    public void setCategory(CourseCategoryBean category) { this.category = category; }

    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
}
