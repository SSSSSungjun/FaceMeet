package com.levelup.FaceMeet.service.match;

import com.levelup.FaceMeet.domain.MatchingPassHistory;
import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.dto.MatchTicketDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.match.MatchingPassHistoryRepository;
import com.levelup.FaceMeet.repository.admin.SettingRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
public class MatchTicketService {

    @Autowired
    private DistributedLockService distributedLockService;
    @Autowired
    private  StringRedisTemplate redisTemplate;

    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private RedisTemplate<String, Setting> settingRedisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchingPassHistoryRepository matchingPassHistoryRepository;

    private static final String TICKET_LOCK_PREFIX = "setting:";

    //매칭권 생성 ( 프론트로 알림 예약을 보내는 시점에 해주면 됨)
    public void createMatchTicket(Long settingId){

        String redisKey = TICKET_LOCK_PREFIX + settingId;
        Setting setting = settingRepository.findById(settingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        // Redis에 저장 (예: 10분 유효)
        settingRedisTemplate.opsForValue().set(redisKey, setting, Duration.ofMinutes(10));

        log.info("매칭권 생성 완료 : id={}, name={}, maxCount={}");
    }

    // Redis 분산 락 + MySQL 사용
    @Transactional // 이 메서드에 트랜잭션이 적용되어 있다고 가정합니다.
    public MatchTicketDTO.MatchTicketResponse acquireTicketWithRedisLock(Long userId, Long settingId) {
        String lockKey = TICKET_LOCK_PREFIX + settingId;

        // 락 획득 후 내부 처리
        return distributedLockService.executeWithLock(lockKey, 10, () -> {
            try {

                return processTicketAcquisition(userId, settingId);
            } catch (RuntimeException e) { // RuntimeException만 명시적으로 잡거나, Exception을 잡고 다시 던집니다.
                log.error("매칭권 획득 처리 중 비즈니스 로직 오류: userId={}, settingId={}, 메시지: {}", userId, settingId, e.getMessage());

                throw e;

            } catch (Exception e) { // 그 외의 예상치 못한 시스템 오류
                log.error("매칭권 획득 처리 중 시스템 오류: userId={}, settingId={}", userId, settingId, e);
                // 시스템 오류는 RuntimeException으로 감싸서 다시 던져 트랜잭션 롤백을 유도합니다.
                throw new RuntimeException("시스템 오류가 발생했습니다.", e);
            }
        });
    }

    // 매칭권 획득 처리 로직 (분산 락 외부)
    // 이 메서드는 @Transactional 어노테이션이 없어도 acquireTicketWithRedisLock에 의해 트랜잭션 컨텍스트 내에서 실행됩니다.
    public MatchTicketDTO.MatchTicketResponse processTicketAcquisition(Long userId, Long settingId) {

        User user = userRepository.findById(userId)
                .orElseThrow( () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Setting settingByDB = settingRepository.findById(settingId).orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        // 0. 종료된 이벤트인지 확인
        if (settingByDB.getEndTime() != null && LocalDateTime.now().isAfter(settingByDB.getEndTime())) {
            throw new CustomException(ErrorCode.MATCHING_PASS_EVENT_ENDED);
        }

        //0. 시작 전 이벤트 인지 확인
        if(settingByDB.getStartTime() != null & LocalDateTime.now().isBefore(settingByDB.getStartTime())){
            System.out.println("settingByDb.getStartTime(): "  +  settingByDB.getStartTime());
            throw new CustomException(ErrorCode.MATHCING_PASS_EVENT_NOTSTARTED);
        }

        // 2. 이미 유저가 획득한 이력이 있는지 확인
        if (Boolean.TRUE.equals(matchingPassHistoryRepository.existsByUserAndSetting(user, settingByDB))) {
            throw new CustomException(ErrorCode.ALREADY_ACQUIRED_MATCHING_PASS);
        }

        // 3. 잔여 수량 확인
        if (settingByDB.getCurrentCnt() <= 0) {
            throw new CustomException(ErrorCode.MATCHING_PASS_SOLD_OUT);
        }

        // 4. 매칭권 획득 이력 저장
        MatchingPassHistory matchingPassHistory = new MatchingPassHistory();
        matchingPassHistory.setAcquiredAt(LocalDateTime.now());
        matchingPassHistory.setUser(user);
        matchingPassHistory.setSetting(settingByDB);
        matchingPassHistory.setIsUsed(false);

        matchingPassHistoryRepository.save(matchingPassHistory); // DB 저장

        // 5. Setting 잔여 수량 감소 (트랜잭션 내에서 업데이트)
        settingByDB.setCurrentCnt(settingByDB.getCurrentCnt() - 1);
        settingRepository.save(settingByDB); // DB 업데이트

        // 6. 응답 반환
        return MatchTicketDTO.MatchTicketResponse.builder()
                .userId(userId)
                .settingId(settingId)
                .message("매칭권 획득 성공")
                .acquiredAt(LocalDateTime.now())
                .remainingCount(settingByDB.getCurrentCnt())
                .sucess(true)
                .build();
    }


}
