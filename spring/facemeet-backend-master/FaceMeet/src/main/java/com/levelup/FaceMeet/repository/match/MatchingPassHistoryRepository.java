package com.levelup.FaceMeet.repository.match;

import com.levelup.FaceMeet.domain.MatchingPassHistory;
import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchingPassHistoryRepository extends JpaRepository<MatchingPassHistory, Long> {
    // User와 Setting으로 존재 여부 확인
    boolean existsByUserAndSetting(User user, Setting setting);

    //사용 안한  매칭권수 확인
    int countByUserAndIsUsedFalse(User user);

    //해당 유저가 사용 안한 매칭권
    Optional<MatchingPassHistory> findFirstByUserAndIsUsedFalse(User user);
}
