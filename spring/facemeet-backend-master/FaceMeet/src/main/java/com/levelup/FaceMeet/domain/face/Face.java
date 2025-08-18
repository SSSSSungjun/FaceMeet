package com.levelup.FaceMeet.domain.face;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "face")
public class Face {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "face_id")
    private Long id;

    @Column(name = "img", length = 150)
    private String img;

    @ManyToOne
    @JoinColumn(name = "faceshape_id", referencedColumnName = "faceshape_id")
    private Faceshape faceShape;

    @ManyToOne
    @JoinColumn(name = "eyebrow_comb_id", referencedColumnName = "eyebrow_comb_id")
    private EyeBrowComb eyebrowComb;

    @ManyToOne
    @JoinColumn(name = "eye_comb_id", referencedColumnName = "eye_comb_id")
    private EyeComb eyeComb;

    @ManyToOne
    @JoinColumn(name = "nose_comb_id", referencedColumnName = "nose_comb_id")
    private NoseComb noseComb;

    @ManyToOne
    @JoinColumn(name = "mouth_comb_id", referencedColumnName = "mouth_comb_id")
    private MouthComb mouthComb;

    @ManyToOne
    @JoinColumn(name = "chin_comb_id", referencedColumnName = "chin_comb_id")
    private ChinComb chinComb;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "summary_analysis", length = 500)
    private String summaryAnalysis;

    @Column(name = "personality", length = 500)
    private String personality;

    @Column(name = "interpersonal_relationships", length = 500)
    private String interpersonalRelationships;

    @Column(name = "career_traits", length = 500)
    private String careerTraits;

    @Column(name = "life_direction", length = 500)
    private String lifeDirection;
}
