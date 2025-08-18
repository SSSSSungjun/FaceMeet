package com.levelup.FaceMeet.repository.user;

import com.levelup.FaceMeet.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndReportedId(Long reporterId, Long reportedId);
}
