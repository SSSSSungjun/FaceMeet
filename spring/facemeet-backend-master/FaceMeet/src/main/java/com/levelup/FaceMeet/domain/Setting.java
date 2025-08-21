package com.levelup.FaceMeet.domain;

import com.levelup.FaceMeet.dto.SettingDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "setting")
@EntityListeners(AuditingEntityListener.class)
public class Setting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false)
    private String title;

    @Column(name = "coupon_count", nullable = false)
    private Integer couponCount;

    @Column(name = "current_cnt", nullable = false)
    private Integer currentCnt;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {

        isActive = true;

        if (currentCnt == null && couponCount != null) {
            currentCnt = couponCount;
        }
    }

    public boolean update(SettingDTO.SettingUpdateRequest request) {
        boolean timeChanged = false;

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            this.title = request.getTitle();
        }

        if (request.getCouponCount() != null) {
            this.couponCount = request.getCouponCount();
        }

        if (request.getStartTime() != null && !request.getStartTime().equals(this.startTime)) {
            this.startTime = request.getStartTime();
            timeChanged = true;
        }

        if (request.getEndTime() != null) {
            this.endTime = request.getEndTime();
        }

        return timeChanged;
    }

    public void softDelete() {
        this.isActive = false;
    }
}