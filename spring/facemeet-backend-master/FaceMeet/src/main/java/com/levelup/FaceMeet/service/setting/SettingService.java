package com.levelup.FaceMeet.service.setting;

import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.dto.SettingDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.admin.SettingRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.service.setting.helper.validator.SettingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final SettingRepository settingRepository;
    private final SettingValidator settingValidator;
    private final UserRepository userRepository;
    private final SettingMessageService settingMessageService;

    /**
     * 세팅 생성
     */
    @Transactional
    public SettingResponse createSetting(Long adminId, SettingCreateRequest request) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 검증
        settingValidator.validateCreation(request);

        Setting setting = Setting.builder()
                .admin(admin)
                .title(request.getTitle())
                .couponCount(request.getCouponCount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Setting savedSetting = settingRepository.save(setting);

        log.info("세팅 생성: adminId={}, title={}", adminId, request.getTitle());

        // 예약 메시지 생성
        settingMessageService.handleSettingCreated(savedSetting.getId(), request);

        return SettingResponse.from(savedSetting);
    }

    /**
     * 세팅 수정
     */
    @Transactional
    public SettingResponse updateSetting(Long settingId, SettingUpdateRequest request) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        // 검증
        settingValidator.validateUpdate(setting, request);

        // 업데이트
        boolean timeChanged = setting.update(request);
        Setting updatedSetting = settingRepository.save(setting);

        // 시간 변경 시 관련 메시지 시간 변경
        if (timeChanged) {
            settingMessageService.handleSettingTimeChanged(settingId, updatedSetting.getStartTime());
        }

        return SettingResponse.from(updatedSetting);
    }

    /**
     * 세팅 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteSetting(Long settingId) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        settingValidator.validateDeletion(setting);

        setting.softDelete();
        settingRepository.save(setting);

        settingMessageService.handleSettingDeleted(settingId);
    }

    /**
     * 특정 세팅 조회
     */
    public SettingResponse getSetting(Long settingId) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        return SettingResponse.from(setting);
    }

    /**
     * 전체 활성화 세팅 목록 조회 (시작시간 최신순)
     */
    public List<SettingResponse> getSettings() {
        return settingRepository.findAllByIsActiveTrueOrderByStartTimeDesc()
                .stream()
                .map(SettingResponse::from)
                .collect(Collectors.toList());
    }
}