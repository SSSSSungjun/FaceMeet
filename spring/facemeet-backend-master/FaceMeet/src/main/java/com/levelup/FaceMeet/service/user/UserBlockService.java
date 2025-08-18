package com.levelup.FaceMeet.service.user;

import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.domain.UserBlock;
import com.levelup.FaceMeet.dto.UserInfoDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.user.UserBlockRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;
    private static final String CHAT_LIST_KEY = "user_chat_list:";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    public boolean isBlockedByPartner(User user , User partner){

        boolean result =userBlockRepository.existsByBlockerAndBlocked(user,partner);
        System.out.println("현재 대상에게 차단되어 있나요 ? " +  result);
        return result;
    }

    public boolean isBlockedByPartner(Long partnerId, Long currentUserId){
        return userBlockRepository.existsByBlocker_IdAndBlocked_Id(partnerId, currentUserId);
    }

    public void createBlock(Long blockerId, Long blockedUserId) {

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "차단 주체가 존재하지 않습니다."));

        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "차단 대상이 존재하지 않습니다."));

        // 이미 차단한 관계인지 체크
        boolean isAlreadyBlocked = userBlockRepository.existsByBlockerAndBlocked(blocker, blockedUser);

        if (isAlreadyBlocked) {
            throw new CustomException(ErrorCode.ALREADY_BLOCKED, "이미 차단된 사용자입니다.");
        }


        UserBlock newUserBlock = UserBlock.builder()
                .blocker(blocker)
                .blocked(blockedUser)
                .build();


        userBlockRepository.save(newUserBlock);
        invalidateUserChatCache(blockerId);
    }

    //회원별 차단 목록 확인
    public List<UserInfoDTO.UserBlockListResponse> getUserBlockList(Long userId) {


        User blocker = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        List<UserBlock> userBlockList = userBlockRepository.findByBlocker(blocker);


        List<User> blockedUsers = userBlockList.stream()
                .map(UserBlock::getBlocked)
                .toList();

        List<UserInfoDTO.UserBlockListResponse> userBlockListDTO = blockedUsers.stream()
                .map(UserInfoDTO.UserBlockListResponse::new)
                .toList();

        return userBlockListDTO;
    }

    //차단 해제
    public void unblockUser(Long blockerId, Long blockedId) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "차단 주체가 존재하지 않습니다."));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "차단 대상이 존재하지 않습니다."));

        UserBlock userBlock = userBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new CustomException(ErrorCode.BLOCK_NOT_FOUND));

        userBlockRepository.delete(userBlock);
        invalidateUserChatCache(blockerId);
    }
    private void invalidateUserChatCache(Long userId) {

        String listKey = CHAT_LIST_KEY + userId;


        redisTemplate.delete(listKey);


    }

}
