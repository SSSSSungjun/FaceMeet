package com.levelup.FaceMeet.service.user;

import com.levelup.FaceMeet.domain.face.Face;
import com.levelup.FaceMeet.dto.CacheDTO.CachedLong;
import com.levelup.FaceMeet.dto.FaceDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.user.FaceRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceService {

    private final UserRepository userRepository;
    private final FaceRepository faceRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "user_face_id", key = "#userId")
    public CachedLong getUserFaceId(Long userId) {
        Long faceId = userRepository.findFaceIdByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_FACE_NOT_FOUND, "해당 유저의 관상 정보가 존재하지 않습니다."));

        return CachedLong.builder().data(faceId).build();
    }

    @CacheEvict(value = "user_face_id", key = "#userId")
    public void evictUserFaceCache(Long userId) {
        log.debug("유저 {}의 관상 정보가 변경되어 캐시를 제거합니다.", userId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="face_info", key="#faceId")
    public FaceInfo getFaceInfo(Long faceId) {

        Face face = faceRepository.findById(faceId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_FACE_NOT_FOUND));

        return FaceInfo.builder()
                .faceId(face.getId())
                .img(face.getImg())
                .faceShapeDesc(face.getFaceShape().getDescription())
                .eyebrowDesc(face.getEyebrowComb().getDesc())
                .eyeDesc(face.getEyeComb().getDesc())
                .noseDesc(face.getNoseComb().getDesc())
                .mouthDesc(face.getMouthComb().getDesc())
                .chinDesc(face.getChinComb().getDesc())
                .title(face.getTitle())
                .description(face.getDescription())
                .summaryAnalysis(face.getSummaryAnalysis())
                .personality(face.getPersonality())
                .interpersonalRelationships(face.getInterpersonalRelationships())
                .careerTraits(face.getCareerTraits())
                .lifeDirection(face.getLifeDirection())
                .build();
    }
}
