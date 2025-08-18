package com.levelup.FaceMeet.repository.user;

import com.levelup.FaceMeet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT u.face.id FROM User u WHERE u.id = :userId AND u.isDeleted = false AND u.face IS NOT NULL")
    Optional<Long> findFaceIdByUserId(@Param("userId") Long userId);

    Optional<User> findBySocialIdAndProvider(String socialId, String provider);
}

