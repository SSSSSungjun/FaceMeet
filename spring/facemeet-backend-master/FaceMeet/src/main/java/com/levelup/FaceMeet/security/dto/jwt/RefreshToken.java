package com.levelup.FaceMeet.security.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Data
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 300)
public class RefreshToken implements Serializable {

    @Id
    private Long userId;
    private String refreshToken;
}
