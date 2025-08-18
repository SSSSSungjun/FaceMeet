package com.levelup.FaceMeet.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(length = 200, nullable = false)
    private String name;
}
