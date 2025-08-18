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
@Table(name = "eye_comb")
public class EyeComb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eye_comb_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "eye_parts_id1", referencedColumnName = "eye_parts_id")
    private EyeParts eyeParts1;

    @ManyToOne
    @JoinColumn(name = "eye_parts_id2", referencedColumnName = "eye_parts_id")
    private EyeParts eyeParts2;

    @ManyToOne
    @JoinColumn(name = "eye_parts_id3", referencedColumnName = "eye_parts_id")
    private EyeParts eyeParts3;

    @Column(length = 500 , name = "`desc`")
    private String desc;
}
