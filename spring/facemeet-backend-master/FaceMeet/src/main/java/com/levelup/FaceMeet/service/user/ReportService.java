package com.levelup.FaceMeet.service.user;

import com.levelup.FaceMeet.domain.ChatRoom;
import com.levelup.FaceMeet.domain.Report;
import com.levelup.FaceMeet.domain.ReportCategory;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.dto.ReportDTO;
import com.levelup.FaceMeet.dto.ReportDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
import com.levelup.FaceMeet.repository.user.ReportCategoryRepository;
import com.levelup.FaceMeet.repository.user.ReportRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public void createReport(Long reporterId, ReportCreateRequest request) {
        if(reportRepository.existsByReporterIdAndReportedId(reporterId, request.getReportedId())) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        System.out.println("=============================신고처리 어케되나 보자 ======================");
        System.out.println("reportedId는 : " + request.getReportedId());
        System.out.println("카테고리 id는 : " + request.getCategoryId());
        System.out.println(request.getReason());
        System.out.println("roomId : " + request.getRoomId());
        System.out.println("img : " + request.getImg());

        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "신고자가 존재하지 않습니다."));

        User reported = userRepository.findById(request.getReportedId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "신고 대상이 존재하지 않습니다."));

        ReportCategory category = reportCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_CATEGORY_NOT_FOUND));

        Report newReport = Report.builder()
                .chatRoom(chatRoom)
                .reportCategory(category)
                .reporter(reporter)
                .reported(reported)
                .reason(request.getReason())
                .img(request.getImg())
                .build();

        reportRepository.save(newReport);
    }

    //관리자가 신고 된 내역 리스트 다 보기
    public List<ReportDTO.ReportListResponse> getReportList(){

        List<Report> reports = reportRepository.findAll();

        List<ReportDTO.ReportListResponse> reportsDTOS  = new ArrayList<>();
        for(Report report: reports ){
            ReportDTO.ReportListResponse response = new ReportDTO.ReportListResponse();
            response.setReportId(report.getId());

            response.setChatRoomId(report.getChatRoom().getId());

            response.setReportCategoryId(report.getReportCategory().getId());
            response.setReportCategoryName(report.getReportCategory().getName());

            response.setReporterId(report.getReporter().getId());
            response.setReporterName(report.getReporter().getName());
            response.setReporterNickName(report.getReporter().getNickname());

            response.setReportedId(report.getReported().getId());
            response.setReportedName(report.getReported().getName());
            response.setReportedNickName(report.getReported().getNickname());

            response.setReason(report.getReason());
            response.setCreatedAt(report.getCreatedAt());
            response.setIsSolved(report.getIsSolved());

            reportsDTOS.add(response);
        }

        return reportsDTOS;
    }

    //신고 처리 완료 하기
    public void resolveReport(Long reportId){
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "해당 신고가 존재하지 않습니다"));

        report.setIsSolved(true);
        reportRepository.save(report);

    }

    //신고 카테고리 목록 전체 조회
    public List<ReportCategory> getReportCategoryList(){
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        return categories;
    }

}
