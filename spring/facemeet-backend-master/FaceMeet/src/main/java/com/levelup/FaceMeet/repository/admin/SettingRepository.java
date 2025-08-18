package com.levelup.FaceMeet.repository.admin;



import com.levelup.FaceMeet.domain.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SettingRepository extends JpaRepository<Setting, Long> {

    List<Setting> findAllByOrderByStartTimeDesc();

    List<Setting> findAllByIsActiveTrueOrderByStartTimeDesc();
}
