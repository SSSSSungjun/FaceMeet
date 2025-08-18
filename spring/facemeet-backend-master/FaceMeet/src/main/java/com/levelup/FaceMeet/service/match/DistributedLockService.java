package com.levelup.FaceMeet.service.match;

import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

// 4. 분산 락 서비스
@Component
@Slf4j
public class DistributedLockService {

    @Autowired
    private  StringRedisTemplate redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    private static final int DEFAULT_EXPIRE_TIME = 10; // 10초

    //락 획득 시도
    public boolean tryLock(String key, String value, int expireSeconds){
        String lockKey =LOCK_PREFIX+key;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, value, Duration.ofSeconds(expireSeconds));

        if(Boolean.TRUE.equals(result)){
            log.debug("Lock acquired : key = {}, value = {}" , lockKey, value);
            return true;
        }

        log.debug("Lock acquisition failed : key = {}" , lockKey);
        return false;
    }

    //락 해제
    public boolean releaseLock(String key, String value) {
        String lockKey = LOCK_PREFIX + key;

        String luaScript = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            else
                return 0
            end
            """;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(lockKey),
                value
        );

        boolean released = result != null && result == 1;
        log.debug("Lock release: key={}, value={}, result={}", lockKey, value, released);
        return released;
    }

    //락 사용하여 작업 실행
    public <T> T executeWithLock(String lockKey, int expireSeconds, Supplier<T> task) {
        String lockValue = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            // 락 획득 재시도 (최대 3회)
            for (int i = 0; i < 3; i++) {
                if (tryLock(lockKey, lockValue, expireSeconds)) {
                    locked = true;
                    break;
                }
                try {
                    Thread.sleep(100L); // 100ms 대기 후 재시도
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CustomException(ErrorCode.LOCK_INTERRUPTED);
                }
            }

            if (!locked) {
                throw new CustomException(ErrorCode.LOCK_ACQUIRE_FAILED);
            }

            return task.get();

        } finally {
            // 락을 성공적으로 획득했을 경우에만 해제
            if (locked) {
                boolean released = releaseLock(lockKey, lockValue);
                if (!released) {
                    log.warn("락 해제 실패: lockKey={}, lockValue={}", lockKey, lockValue);
                }
            }
        }
    }


    public void executeWithLock(String lockKey, int expireSeconds, Runnable task) {
        executeWithLock(lockKey, expireSeconds, () -> {
            task.run();
            return null;
        });
    }

}
