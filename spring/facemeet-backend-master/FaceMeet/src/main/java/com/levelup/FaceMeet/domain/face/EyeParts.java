package com.levelup.FaceMeet.domain.face;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eye_parts")
public class EyeParts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eye_parts_id")
    private Long id;
    private String keyword;
    @Column(name = "`desc`")
    private String desc;
}
