package com.levelup.FaceMeet.controller.user;

import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.user.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
@Tag(name = "fileController", description = "s3 presingedurl을 관리합니다")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/presingedurl/face")
    @Operation(summary = "presingedUrl 발급", description = "관상 사진을 저장할 presingedurl을 저장합니다")
    public ResponseEntity<List<String>> getPresigndUrl(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long reporterId = userDetails.getUserId();

        List<String> urls = fileService.getPreSigned3Url(reporterId.toString());

        return ResponseEntity.ok(urls);
    }
}
