package com.levelup.FaceMeet.exception;

import com.google.api.Http;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ======================================
    // COMMON ERRORS (공통 에러)
    // ======================================

    /**
     * 400 BAD_REQUEST - 입력값 검증 오류
     */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_01", "잘못된 입력값입니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "COMMON_400_02", "필수 입력값이 누락되었습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_03", "잘못된 타입의 값입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),

    /**
     * 404 NOT_FOUND - 공통 리소스 없음
     */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_01", "요청한 리소스를 찾을 수 없습니다."),

    /**
     * 409 CONFLICT - 중복 리소스
     */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON_409_01", "이미 존재하는 리소스입니다."),

    /**
     * 500 INTERNAL_SERVER_ERROR - 서버 공통 오류
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_01", "서버 내부 오류가 발생했습니다."),

    /**
     * 502
     */
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "SERVER_503", "게이트웨이 오류가 발생했습니다."),

    /**
     * 503
     */
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVER_503", "현재 서버를 사용할 수 없습니다."),

    // ======================================
    // AUTHENTICATION & AUTHORIZATION (인증/인가)
    // ======================================

    /**
     * 400 BAD_REQUEST - 인증 제공자 오류
     */
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_01", "지원하지 않는 Provider 입니다."),

    /**
     * 401 UNAUTHORIZED - 인증 실패
     */
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AUTH_401_01", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_02", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_03", "만료된 토큰입니다."),

    /**
     * 403 FORBIDDEN - 접근 권한 없음
     */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403_01", "접근 권한이 없습니다."),

    // ======================================
    // USER MANAGEMENT (사용자 관리)
    // ======================================

    /**
     * 404 NOT_FOUND - 사용자 없음
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_01", "사용자를 찾을 수 없습니다."),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_404_02", "관리자가 존재하지 않습니다."),

    /**
     * 409 CONFLICT - 사용자 중복
     */
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, "USER_409_01", "이미 존재하는 이메일입니다."),

    OAUTH2_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER_500_01", "소셜 로그인 연동 해제 중 오류가 발생했습니다."),

    ALREADY_BLOCKED(HttpStatus.INTERNAL_SERVER_ERROR, "USER_500_02","이미 차단된 사용자입니다ㅑ"),
    // ======================================
    // FACE (관상)
    // ======================================

    /**
     * 404 NOT_FOUND - 관상 없음
     */
    USER_FACE_NOT_FOUND(HttpStatus.NOT_FOUND, "FACE_404_01", "해당 관상 정보를 찾을 수 없습니다."),

    // ======================================
    // MATCHING SYSTEM (매칭 시스템)
    // ======================================

    /**
     * 404 NOT_FOUND - 매칭 없음
     */
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_404_01", "해당 매칭 정보를 찾을 수 없습니다."),

    /**
     * 409 CONFLICT - 매칭 관련 충돌
     */
    ALREADY_ACQUIRED_MATCHING_PASS(HttpStatus.CONFLICT, "MATCH_409_01", "이미 매칭권을 획득한 사용자입니다."),
    MATCHING_PASS_SOLD_OUT(HttpStatus.CONFLICT, "MATCH_409_02", "매칭권이 모두 소진되었습니다."),
    MATCHING_RESPONSE_ERROR(HttpStatus.CONFLICT, "MATCH_409_03" , "매칭할 상대가 없습니다"),
    MATCHING_PASS_EVENT_ENDED(HttpStatus.CONFLICT, "MATCH_409_04" ,"이미 종료된 이벤트입니다"),
    MATHCING_PASS_EVENT_NOTSTARTED(HttpStatus.CONFLICT, "MATCH_409_05" , "아직 시작되지 않은 이벤트입니다"),
    // ======================================
    // CHAT SYSTEM (채팅 시스템)
    // ======================================

    /**
     * 404 NOT_FOUND - 채팅방 없음
     */
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_404_01", "해당 채팅방이 존재하지 않습니다."),
    CHATROOM_ALEADY_EXIST(HttpStatus.NOT_FOUND, "CHATROOM_404_01", "이미 존재하는 채팅방입니다"),
    INVALID_PAGE_REQUEST(HttpStatus.NOT_FOUND , "CHATROOM_404_01", "페이지 수를 초과하였습니다" ),
    CANT_CHAT_STATUS(HttpStatus.NOT_FOUND, "CHATROOM_404_02" , "현재 채팅이 불가능한 채팅방 입니다."),
    CANT_CHAT_OURS(HttpStatus.NOT_FOUND, "CHATROOM_404_02" , "당신이 포함되어있는 채팅방이 아닙니다"),
    // ======================================
    // REPORT & BLACKLIST SYSTEM (신고 및 블랙리스트)
    // ======================================

    /**
     * 404 NOT_FOUND - 신고 관련 리소스 없음
     */
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_01", "신고 내역이 존재하지 않습니다."),
    REPORT_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_CATE_404_01", "신고 카테고리가 존재하지 않습니다."),
    BLACK_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "BLACK_CATE_404_01", "블랙리스트 카테고리가 존재하지 않습니다."),
    BLACKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "BLACKLIST_404_01", "해당 블랙리스트가 존재하지 않습니다."),
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK_404_01", "차단 내역이 존재하지 않습니다."),

    /**
     * 409 CONFLICT - 중복
     */
    ALREADY_BLACKLISTED(HttpStatus.CONFLICT, "BLACKLIST_409_01", "이미 블랙리스트에 등록된 유저입니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "REPORT_409_01", "이미 신고된 사용자입니다."),

    // ======================================
    // FCM & NOTIFICATION SYSTEM (푸시 알림)
    // ======================================

    /**
     * 404 NOT_FOUND - FCM 리소스 없음
     */
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM_TOKEN_404_01", "디바이스 토큰을 찾을 수 없습니다."),
    FCM_TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM_TOPIC_404_01", "토픽을 찾을 수 없습니다."),
    SCHEDULED_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCH_MSG_404", "예약된 메시지를 찾을 수 없습니다."),

    /**
     * 409 CONFLICT - 토픽 중복
     */
    DUPLICATE_TOPIC_NAME(HttpStatus.CONFLICT, "TOPIC_409_01", "이미 존재하는 토픽 이름입니다."),
    ALREADY_SUBSCRIBED_TOPIC(HttpStatus.CONFLICT, "TOPIC_409_02", "이미 구독중인 토픽입니다."),
    NOT_SUBSCRIBED_TOPIC(HttpStatus.CONFLICT, "TOPIC_409_03", "구독하지 않은 토픽입니다."),

    /**
     * 500 INTERNAL_SERVER_ERROR - FCM 서버 오류
     */
    TOPIC_SUBSCRIPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_500_01", "토픽 구독 처리 중 오류가 발생했습니다."),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_500_02", "FCM 메시지 전송 중 오류가 발생했습니다."),
    FCM_PRE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_500_03", "사전 알림 전송에 실패했습니다."),
    MESSAGE_SCHEDULING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_500_04", "사전 알림 예약에 실패했습니다."),
    // ======================================
    // SETTING SYSTEM (설정 시스템)
    // ======================================

    /**
     * 400 BAD_REQUEST - 설정 값 오류
     */
    INVALID_SETTING_COUNT(HttpStatus.BAD_REQUEST, "SETTING_COUNT_400_01", "유효하지 않은 쿠폰 개수 값입니다."),

    /**
     * 404 NOT_FOUND - 설정 없음
     */
    SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTING_404_01", "해당 세팅이 존재하지 않습니다."),

    //매칭권을 다썻을때
    MATCHING_TICKET_EXHAUSTED(HttpStatus.GONE, "MATCH_410_01", "매칭 가능한 횟수를 모두 소진하셨습니다."),

    /**
     * 409 CONFLICT - 설정 작업 불가
     */
    SETTING_CONFLICT(HttpStatus.CONFLICT, "SETTING_409_01", "해당 세팅은 요청에 대한 작업이 불가능합니다."),

    // ======================================
    // CONCURRENCY CONTROL (동시성 제어)
    // ======================================

    /**
     * 500 INTERNAL_SERVER_ERROR - 락 처리 오류
     */
    LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "LOCK_500_01", "락 획득 재시도 중 인터럽트가 발생했습니다."),
    LOCK_ACQUIRE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LOCK_500_02", "락 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");




    private final HttpStatus status;
    private final String code;
    private final String message;
}
