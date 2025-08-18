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
@Table(name = "eyebrow_comb")
public class EyeBrowComb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eyebrow_comb_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "eyebrow_parts_id1", referencedColumnName = "eyebrow_parts_id")
    private EyebrowParts eyebrowParts1;
    @ManyToOne
    @JoinColumn(name = "eyebrow_parts_id2", referencedColumnName = "eyebrow_parts_id")
    private EyebrowParts eyebrowParts2;
    @ManyToOne
    @JoinColumn(name = "eyebrow_parts_id3", referencedColumnName = "eyebrow_parts_id")
    private EyebrowParts eyebrowParts3;
    @ManyToOne
    @JoinColumn(name = "eyebrow_parts_id4", referencedColumnName = "eyebrow_parts_id")
    private EyebrowParts eyebrowParts4;



    @Column(length = 500 ,name = "`desc`")
    private String desc;
}
