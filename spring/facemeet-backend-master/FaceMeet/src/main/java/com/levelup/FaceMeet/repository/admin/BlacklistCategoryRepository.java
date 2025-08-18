package com.levelup.FaceMeet.repository.admin;

import com.levelup.FaceMeet.domain.BlacklistCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface BlacklistCategoryRepository extends JpaRepository<BlacklistCategory, Long> {
}
