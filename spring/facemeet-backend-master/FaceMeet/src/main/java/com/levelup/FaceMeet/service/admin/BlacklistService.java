package com.levelup.FaceMeet.service.admin;

import com.levelup.FaceMeet.domain.*;
import com.levelup.FaceMeet.dto.BlacklistDTO;
import com.levelup.FaceMeet.dto.BlacklistDTO.*;
import com.levelup.FaceMeet.dto.ChatRoomDTO;
import com.levelup.FaceMeet.dto.MessageDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.admin.BlacklistCategoryRepository;
import com.levelup.FaceMeet.repository.admin.BlacklistRepository;
import com.levelup.FaceMeet.repository.chat.ChatRoomMemberRepository;
import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
import com.levelup.FaceMeet.repository.chat.MessageRepository;
import com.levelup.FaceMeet.repository.user.ReportRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.service.user.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final BlacklistRepository blacklistRepository;
    private final BlacklistCategoryRepository blacklistCategoryRepository;
    private final UserBlockService userBlockService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;


    public void createReport(Long adminId, BlacklistRequest request) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        Report report = reportRepository.findById(request.getReportId())
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        User user = report.getReported();

        boolean exists = blacklistRepository.existsByUserId(user.getId());
        if (exists) {
            throw new CustomException(ErrorCode.ALREADY_BLACKLISTED);
        }

        BlacklistCategory category = blacklistCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.BLACK_CATEGORY_NOT_FOUND));

        Blacklist newBlacklist = Blacklist.builder()
                .user(user)
                .report(report)
                .admin(admin)
                .category(category)
                .build();

        blacklistRepository.save(newBlacklist);

        //신고 처리 완료
        report.setIsSolved(true);
        reportRepository.save(report);

    }

    //블랙리스트 목록 가져오기
    public List<BlacklistDTO.BlackListResponse> getBlackList(){
        List<Blacklist> blacklistList = blacklistRepository.findAll();

        List<BlacklistDTO.BlackListResponse> dtos = new ArrayList<>();

        for(Blacklist blacklist : blacklistList){

            BlacklistDTO.BlackListResponse dto = new BlacklistDTO.BlackListResponse();
            dto.setBlacklistId(blacklist.getId());

            dto.setUserId(blacklist.getUser().getId());
            dto.setUserName(blacklist.getUser().getName());
            dto.setProvider(blacklist.getUser().getProvider());

            dto.setBlackListCategoryId(blacklist.getCategory().getId());
            dto.setBlackListCategoryName(blacklist.getCategory().getName());

            dto.setCreatedAt(blacklist.getCreatedAt());
            dtos.add(dto);
        }

        return dtos;
    }

    public void deleteBlackList(Long blackListId){
        Blacklist blacklist = blacklistRepository.findById(blackListId)
                .orElseThrow(() -> new CustomException(ErrorCode.BLACKLIST_NOT_FOUND));

        blacklistRepository.delete(blacklist);
    }

    public List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> getUserChatRoomList(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //회원별 나가지 않은 채팅방
        List< ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findByUserAndIsVisible(user , true);

        System.out.println("chatRoomMemberList : " + chatRoomMemberList);

        List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> resultList = new ArrayList<>();
        for(ChatRoomMember member  : chatRoomMemberList){

            ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto = new ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse();

            //상대방이 누구인지 찾기
            ChatRoom chatRoom = member.getRoom();
            User partner = chatRoom.getUser1().getId() == userId ? chatRoom.getUser2() : chatRoom.getUser1();

            //보여줘야 할 상대방 정보 저장
            dto.setUserId(partner.getId());
            dto.setNickName(partner.getNickname());
            dto.setLastActivatedTime(partner.getLastSeen());
            dto.setIsOnline(partner.getIsOnline());
            dto.setImgUrl(partner.getFace() == null ? null : partner.getFace().getImg());

            //보여줄 채팅방 정보 설정
            dto.setChatRoomId(chatRoom.getId());
            dto.setChatRoomStringId(chatRoom.getRoomStringId());
            MessageDTO.ChatSummaryResponse summary = getLastMessageAndUnreadCount(chatRoom.getId(), partner.getId());
            System.out.println("summary : " + summary);
            if(summary == null){
                dto.setLastMessage(null);
                dto.setNonReadCnt(0L);
            } else {
                // summary가 null이 아닐 때만 접근
                dto.setLastMessage(summary.getLastMessage() != null ? summary.getLastMessage().toString() : null);
                dto.setLastSendMessageTime(summary.getLastMessageTime());
                dto.setNonReadCnt(summary.getTotalUnreadCountFromPartner());
            }
            System.out.println("summary : " + summary);
            //내가 차단을 했는지  +  차단을 당했는디
            dto.setBlocked(userBlockService.isBlockedByPartner(user, partner));
            dto.setDeleted(user.getIsDeleted());

            resultList.add(dto);


        }

        //마지막으로 보낸 메시지 시간 기준으로 정렬
        resultList.sort((dto1, dto2) -> {
            LocalDateTime time1 = dto1.getLastSendMessageTime();
            LocalDateTime time2 = dto2.getLastSendMessageTime();

            // null 처리: 최신 메시지가 없는 경우 맨 뒤로 보냄
            if (time1 == null && time2 == null) return 0;
            if (time1 == null) return 1;
            if (time2 == null) return -1;

            return time2.compareTo(time1); // 최신 순 정렬 (내림차순)
        });

        return resultList;

    }

    private MessageDTO.ChatSummaryResponse getLastMessageAndUnreadCount(Long roomId, Long partnerId) {
        // DB에 메시지가 하나도 없다면 null 반환
        long messageCountInDB = messageRepository.countByRoom_Id(roomId);
        if (messageCountInDB == 0) {
            return null;
        }

        // 안 읽은 메시지 수 조회
        long unreadCountFromDB = messageRepository.countByRoom_IdAndSender_IdAndIsReadFalse(roomId, partnerId);
        System.out.println("DB에서 안 읽은 메시지 수: " + unreadCountFromDB);

        // 마지막 메시지 조회
        Message message = messageRepository.findTopByRoom_IdOrderBySendAtDesc(roomId)
                .orElseThrow(() -> new IllegalStateException("메시지가 존재해야 하는데 조회되지 않음"));


        // Message → MessageSendResponse 변환 (objectMapper 사용 시)
//        MessageDTO.MessageSendResponse lastMessage = objectMapper.convertValue(message, MessageDTO.MessageSendResponse.class);

        return new MessageDTO.ChatSummaryResponse(message.getContent(), message.getSendAt(),unreadCountFromDB);
    }



    public List<BlacklistCategory> getBlacklistCategories() {
        return blacklistCategoryRepository.findAll();
    }
}
