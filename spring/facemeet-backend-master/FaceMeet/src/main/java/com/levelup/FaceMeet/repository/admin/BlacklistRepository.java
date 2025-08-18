package com.levelup.FaceMeet.repository.admin;

import com.levelup.FaceMeet.domain.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Optional<Blacklist> findByUserId(Long userId);

    boolean existsByUserId(Long id);
}
