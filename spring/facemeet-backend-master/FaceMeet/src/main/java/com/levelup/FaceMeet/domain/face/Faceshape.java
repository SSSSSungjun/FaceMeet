package com.levelup.FaceMeet.domain.face;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "faceshape")
public class Faceshape {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faceshape_id")
    private Long id;

    @Column(name ="keyword" , nullable=false, length = 15)
    private String keyword;


    @Column(name = "desc", nullable = false, length = 100)
    private String description;
}