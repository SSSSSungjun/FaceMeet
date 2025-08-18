package com.levelup.FaceMeet.repository.user;

import com.levelup.FaceMeet.domain.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Long> {
}
