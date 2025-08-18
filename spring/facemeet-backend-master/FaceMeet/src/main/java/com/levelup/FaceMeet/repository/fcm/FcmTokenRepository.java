package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByDeviceToken(String token);

    List<FcmToken> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT f FROM FcmToken f WHERE f.userId = :userId AND f.deviceId = :deviceId " +
            "AND f.deviceToken != :excludeToken AND f.isActive = true")
    List<FcmToken> findActiveTokensByUserIdAndDeviceIdExcluding(
            @Param("userId") Long userId,
            @Param("deviceId") String deviceId,
            @Param("excludeToken") String excludeToken);

    @Query("SELECT f FROM FcmToken f WHERE f.userId = :userId AND f.deviceId = :deviceId AND f.isActive = true")
    Optional<FcmToken> findActiveTokenByUserIdAndDeviceId(
            @Param("userId") Long userId,
            @Param("deviceId") String deviceId);

//    @Query("SELECT COUNT(f) FROM FcmToken f WHERE f.userId = :userId AND f.isActive = true")
//    int countActiveTokensByUserId(@Param("userId") Long userId);
//
//    @Modifying
//    @Query("UPDATE FcmToken f SET f.isActive = false, f.deactivatedAt = CURRENT_TIMESTAMP " +
//            "WHERE f.userId = :userId AND f.deviceId = :deviceId AND f.deviceToken != :currentToken")
//    void deactivateOldTokensForDevice(
//            @Param("userId") Long userId,
//            @Param("deviceId") String deviceId,
//            @Param("currentToken") String currentToken);
//
//    @Query("SELECT COUNT(f) FROM FcmToken f WHERE f.isActive = true")
//    long countAllActiveTokens();
}
