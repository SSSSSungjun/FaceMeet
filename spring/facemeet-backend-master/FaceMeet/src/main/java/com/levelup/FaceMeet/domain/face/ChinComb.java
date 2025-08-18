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
@Table(name = "chin_comb")
public class ChinComb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chin_comb_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chin_parts_id1", referencedColumnName = "chin_parts_id")
    private ChinParts chinParts1;

    @ManyToOne
    @JoinColumn(name = "chin_parts_id2", referencedColumnName = "chin_parts_id")
    private ChinParts chinParts2;



    @Column(name = "`desc`" ,length = 500)
    private String desc;
}
