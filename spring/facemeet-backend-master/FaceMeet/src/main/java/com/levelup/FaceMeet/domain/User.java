package com.levelup.FaceMeet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.levelup.FaceMeet.domain.face.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 250)
    private String email;

    @Column(length = 50)
    private String name;

    @Column(length = 45)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 200)
    private String address;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "is_online")
    private Boolean isOnline;

    private LocalDateTime birth;

    @Column(name = "prefer_age_upper")
    private Integer preferAgeUpper;

    @Column(name = "prefer_age_lower")
    private Integer preferAgeLower;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id")
    @JsonIgnore
    @ToString.Exclude  // Lombok toString에서 제외
    private Face face;

    public enum Role {
        USER, ADMIN
    }

    public enum Gender {
        m, f, u
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
        this.role = Role.USER;
    }

    public void delete() {
        this.isDeleted = true;
        this.email = "deleted_user@facemeet.deleted";
        this.name = null;
        this.nickname = "(알 수 없음)";
        this.address = null;
        this.latitude = null;
        this.longitude = null;
        this.birth = null;
        this.preferAgeUpper = null;
        this.preferAgeLower = null;
        this.gender = Gender.u;
        this.isOnline = null;
        this.lastSeen = null;
        this.socialId = "deleted_user@facemeet.deleted";
        this.provider = "deleted_user@facemeet.deleted";
    }
}