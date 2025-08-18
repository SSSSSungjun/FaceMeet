package com.levelup.FaceMeet.repository.match;

import com.levelup.FaceMeet.domain.Matching;
import com.levelup.FaceMeet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    //잔여 매칭권수 확인을 위해 해당 유저가 오늘 매칭을 했는지
    boolean existsByRequesterAndCreatedAtBetween(User requester, LocalDateTime start, LocalDateTime end);

    //user1과 user2로 매칭 결과를 차음
    Matching findByRequesterAndAccepter(User requester, User accepter);

    @Query("SELECT m FROM Matching m WHERE " +
            "(m.requester.id = :userId1 AND m.accepter.id = :userId2) OR " +
            "(m.requester.id = :userId2 AND m.accepter.id = :userId1)")
    Optional<Matching> findByTwoUserId(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
