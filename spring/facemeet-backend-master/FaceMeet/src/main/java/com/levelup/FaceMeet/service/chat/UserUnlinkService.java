package com.levelup.FaceMeet.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUnlinkService {

//    public void unlinkKakao(String accessToken) {
//        String url = "https://kapi.kakao.com/v1/user/unlink";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        try {
//            restTemplate.postForEntity(url, entity, String.class);
//        } catch (HttpClientErrorException e) {
//            log.warn("카카오 unlink 실패: {}", e.getResponseBodyAsString());
//        }
//    }

}
