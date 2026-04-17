package com.ctbc.assignment2.bean;

import java.util.Date;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 課程實體類別 (Entity)
 * @Entity: 標記此類別為 JPA 實體，表示它會映射到資料庫的一張表。
 * @Table: 指定對應的資料庫資料表名稱為 "course"。
 */
@Entity
@Table(name = "course")
public class CourseBean {

    // @Id: 標記此欄位為資料表的主鍵 (Primary Key)
    // @GeneratedValue: 設定主鍵的生成策略，IDENTITY 表示交由資料庫自動遞增 (Auto Increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank: 驗證欄位字串不能為 null 且去除前後空白後必須有內容 (Spring Validation)
    @NotBlank(message = "課程名稱不可為空")
    private String courseName;

    // @NotNull: 驗證欄位不能為 null
    // @PositiveOrZero: 驗證數字必須是大於等於 0 的正數或零
    @NotNull(message = "價格不可為空")
    @PositiveOrZero(message = "價格不可為負數")
    private Double price;

    @Column(length = 500)
    private String courseSummary;

    @Lob
    private String courseDescription;

    @Column(length = 512)
    private String coverImageUrl;

    @Column(length = 120)
    private String instructorName;

    @PositiveOrZero(message = "課程時數不可為負數")
    private Integer durationHours;

    @Column(length = 60)
    private String level;

    // @ManyToOne: 定義「多對一」的資料表關聯 (多個課程可以屬於同一個類別)
    // @JoinColumn: 定義關聯的外鍵 (Foreign Key) 欄位名稱設定為 "category_id"
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private CourseCategoryBean category;

    // @Temporal: 告訴 JPA 這個 Date 型別要對應到資料庫的 TIMESTAMP (包含日期與時間)
    // @Column(updatable = false): 控制此欄位在未來更新 (Update) 操作時不會被修改
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // @PrePersist: JPA 的實體生命週期回呼 (Callback)，在資料首次寫入資料庫前 (Insert) 會自動執行此方法
    @PrePersist
    public void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    // @PreUpdate: 在資料更新寫入資料庫前 (Update) 會自動執行此方法
    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public CourseCategoryBean getCategory() { return category; }
    public void setCategory(CourseCategoryBean category) { this.category = category; }

    public String getCourseSummary() { return courseSummary; }
    public void setCourseSummary(String courseSummary) { this.courseSummary = courseSummary; }

    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
}
