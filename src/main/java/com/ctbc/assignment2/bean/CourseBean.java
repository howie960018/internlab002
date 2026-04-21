package com.ctbc.assignment2.bean;

import java.util.Date;
import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.*;

/**
 * 課程資料模型 (Entity)
 * 代表資料庫中的課程表格。
 */
@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "category") // 避免印出字串時發生無限迴圈  
//明確告訴 Lombok：「印 CourseBean 的資訊時，印出 id、名稱、價格就好，千萬不要印出 category

// 告訴 Lombok 產生 equals/hashCode 時，只看有標記 @Include 的欄位
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // 只使用 ID 來判斷是不是「同一堂課」。
    // 這樣就算修改了價格或名稱，這堂課在記憶體中的身分依然不變，能避免 Hibernate 追蹤資料時發生 Bug
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank(message = "課程名稱不可以為空")
    private String courseName;

    @NotNull(message = "價格不可以為空")
    @PositiveOrZero(message = "價格不可以為負數")
    private Double price;

    // 多對一關聯：多個「課程」可以屬於一個「類別」
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)  //告訴資料庫：「請在 course 這張實體資料表裡面，建立一個名為 category_id 的欄位（外鍵）
    // 當關聯的「類別」被刪除時，這裡的 category 會自動變成 null，保護課程資料不被連帶刪除。
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CourseCategoryBean category; 

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false) // 建立時間一旦寫入就不允許修改
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // 自動打卡機：資料第一次存入資料庫前，自動觸發此方法記錄時間
    @PrePersist
    public void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    // 自動打卡機：資料每次更新前，自動觸發此方法更新時間
    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }
}