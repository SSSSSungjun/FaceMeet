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
@Table(name = "mouth_comb")
public class MouthComb {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mouth_comb_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mouth_parts_id1", referencedColumnName = "mouth_parts_id")
    private MouthParts mouthParts1;

    @ManyToOne
    @JoinColumn(name = "mouth_parts_id2", referencedColumnName = "mouth_parts_id")
    private MouthParts mouthParts2;

    @ManyToOne
    @JoinColumn(name = "mouth_parts_id3", referencedColumnName = "mouth_parts_id")
    private MouthParts mouthParts3;

    @Column(length = 500, name = "`desc`")
    private String desc;
}
