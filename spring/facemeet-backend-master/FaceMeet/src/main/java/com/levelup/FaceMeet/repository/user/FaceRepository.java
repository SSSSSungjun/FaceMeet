package com.levelup.FaceMeet.repository.user;

import com.levelup.FaceMeet.domain.face.Face;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceRepository extends JpaRepository<Face, Long> {
}