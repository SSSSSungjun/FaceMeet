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
@Table(name = "nose_comb")
public class NoseComb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nose_comb_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nose_parts_id1", referencedColumnName = "nose_parts_id")
    private NoseParts noseParts1;

    @ManyToOne
    @JoinColumn(name = "nose_parts_id2", referencedColumnName = "nose_parts_id")
    private NoseParts noseParts2;

    @ManyToOne
    @JoinColumn(name = "nose_parts_id3", referencedColumnName = "nose_parts_id")
    private NoseParts noseParts3;

    @ManyToOne
    @JoinColumn(name = "nose_parts_id4", referencedColumnName = "nose_parts_id")
    private NoseParts noseParts4;

    @Column(length = 500 ,name = "`desc`")
    private String desc;
}
