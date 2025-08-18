package com.levelup.FaceMeet.service.match;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.domain.ChatRoom;
import com.levelup.FaceMeet.domain.Matching;
import com.levelup.FaceMeet.domain.MatchingPassHistory;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.dto.MatchDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.chat.ChatRoomMemberRepository;
import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
import com.levelup.FaceMeet.repository.chat.MessageRepository;
import com.levelup.FaceMeet.repository.match.MatchingPassHistoryRepository;
import com.levelup.FaceMeet.repository.match.MatchingRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.service.chat.ChatRoomService;
import com.levelup.FaceMeet.service.chat.ChatService;
import com.levelup.FaceMeet.service.user.UserBlockService;
import com.levelup.FaceMeet.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class MatchingService {

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private MatchingPassHistoryRepository matchingPassHistoryRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private  RedisTemplate<String, String> redisTemplate;


    // Redis 키 상수
    private static final String CHAT_LIST_KEY = "user_chat_list:";

    //잔여 매칭 권수 확인
    public int getRemainMathchingCnt(Long userPk){

        int remainCnt  = 0;

        User user = userRepository.findById(userPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        //오늘 매칭을 했는지
        boolean hasMatchedToday = matchingRepository.existsByRequesterAndCreatedAtBetween(user, startOfDay, endOfDay);
        if(!hasMatchedToday)
            remainCnt ++;

        int getCnt = matchingPassHistoryRepository.countByUserAndIsUsedFalse(user);

        return remainCnt + getCnt;

    }

    //매칭결과로 채팅방 아이디만 넘겨주기
    public MatchDTO.MathchingSucessResponseChatRoomId getChatRoomId(Long userPk) {
        MatchDTO.MathchingSucessResponseChatRoomId sucessResponse = new MatchDTO.MathchingSucessResponseChatRoomId();

        User user = userRepository.findById(userPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        boolean hasMatchedToday = matchingRepository.existsByRequesterAndCreatedAtBetween(user, startOfDay, endOfDay);
    //
        if (hasMatchedToday) {
            int getCnt = matchingPassHistoryRepository.countByUserAndIsUsedFalse(user);
            if (getCnt == 0)
                throw new CustomException(ErrorCode.MATCHING_TICKET_EXHAUSTED, "매칭 권이 소진되었습니다");

            Optional<MatchingPassHistory> optionalPass = matchingPassHistoryRepository.findFirstByUserAndIsUsedFalse(user);
            if (optionalPass.isPresent()) {
                MatchingPassHistory pass = optionalPass.get();
                pass.setIsUsed(true);
                pass.setUsedAt(LocalDateTime.now());
                matchingPassHistoryRepository.save(pass);  // 변경사항 저장
            } else {
                throw new CustomException(ErrorCode.MATCHING_TICKET_EXHAUSTED, "사용 가능한 매칭권이 없습니다.");
            }

        }

        RestTemplate restTemplate = new RestTemplate();
        String djangoUrl = "https://i13d201.p.ssafy.io/django/articles/recommend/"+userPk+"/";



        try {

            MatchDTO.MatchResponse result;
            ResponseEntity<MatchDTO.MatchResponse> response =
                    restTemplate.getForEntity(djangoUrl, MatchDTO.MatchResponse.class);

            result = response.getBody();


            System.out.println("장고 : " + result);
            Long matchedUserId = result.getMatchUserId();
            Double similar = result.getSimilarity();

            if(similar == 0)
                similar += 100;
            else
                similar *= 100;

            System.out.println("매칭률 : " + similar);

            User matedUser = userRepository.findById(matchedUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "매칭 상대가 존재하지 않습니다"));




            ChatRoom chatRoom = chatService.getOrCreateChatRoom(user.getId(), matedUser.getId());


            sucessResponse.setChatRoomId(chatRoom.getId());

            Matching matching = new Matching();
            matching.setRequester(user);
            matching.setAccepter(matedUser);
            matching.setCreatedAt(LocalDateTime.now());
            matching.setCompatibility(BigDecimal.valueOf(similar));
            matchingRepository.save(matching);



//            invalidateUserChatCache(userPk);

            // 아래 두 값에서 NPE 등 발생 가능성 대비

        } catch (RestClientException e) {
            // HTTP 요청 실패 등 처리
            throw new CustomException(ErrorCode.MATCHING_RESPONSE_ERROR, "매칭 상대가 더이상 존재하지 않습니다");
        }

        return sucessResponse;
    }

    /**
     * 궁합도 정보
     */
    @Cacheable(value = "compatibility", key = "T(Math).min(#requesterId, #partnerId) + '_' + T(Math).max(#requesterId, #partnerId)")
    public BigDecimal getCompatibility(Long requesterId, Long partnerId) {
        Matching matching = matchingRepository.findByTwoUserId(requesterId, partnerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        return matching.getCompatibility();
    }

    /**
     * 사용자의 채팅방 캐시 무효화
     */
    private void invalidateUserChatCache(Long userId) {

            String listKey = CHAT_LIST_KEY + userId;


            redisTemplate.delete(listKey);


    }
}
