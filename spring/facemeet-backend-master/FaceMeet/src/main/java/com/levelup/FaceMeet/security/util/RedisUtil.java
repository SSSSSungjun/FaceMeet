package com.levelup.FaceMeet.security.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> stringValueRedisTemplate;

    public void save(String key, String value) {
        stringValueRedisTemplate.opsForValue().set(key, value);
    }

    // TTL 설정
    public void save(String key, String value, Duration ttl) {
        stringValueRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(stringValueRedisTemplate.opsForValue().get(key));
    }

    public void delete(String key) {
        stringValueRedisTemplate.delete(key);
    }
}
