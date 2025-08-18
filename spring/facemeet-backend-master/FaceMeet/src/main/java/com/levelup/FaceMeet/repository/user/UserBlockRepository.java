package com.levelup.FaceMeet.repository.user;

import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    // blocker = partner, blocked = currentUser 인 데이터 존재 여부 확인
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    //해당 사용자가 차단한 사용자들 확인
    List<UserBlock> findByBlocker(User blocker);

    //차단 해제를 위해 해당 차단 열을 찾기
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);

  //id 기준 차단 확인
  boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);



}
