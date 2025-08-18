package com.levelup.FaceMeet.security.service;

import com.levelup.FaceMeet.domain.Blacklist;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.repository.admin.BlacklistRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.security.dto.oauth2.OAuth2Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final BlacklistRepository blacklistRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 기본 OAuth2UserService 객체 생성
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        // 클라이언트 등록 ID(naver, kakao)와 사용자 이름 속성을 가져온다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // OAuth2UserService를 사용하여 가져온 OAuth2User 정보로 OAuth2Attribute 객체를 만든다.
        OAuth2Attribute oAuth2Attribute =
                OAuth2Attribute.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Map<String, Object> userAttribute = oAuth2Attribute.convertToMap();

        // 사용자의 소셜 로그인 제공자와 소셜 식별자로 기존 회원 여부 조회
        String socialId = (String) userAttribute.get("socialId");
        String provider = (String) userAttribute.get("provider");

        Optional<User> findUser = userRepository.findBySocialIdAndProvider(socialId, provider);

        // 첫 가입 회원일 경우
        if (findUser.isEmpty()) {
            log.debug("신규 회원 회원가입 시도: socialId={}, provider={}", socialId, provider);
            userAttribute.put("exist", false);
            // 회원의 권한(회원이 존재하지 않으므로 기본권한인 ROLE_USER를 넣어준다), 회원속성, 속성이름을 이용해 DefaultOAuth2User 객체를 생성해 반환한다.
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    userAttribute, "socialId");
        }
        else {
            User user = findUser.get();

            Optional<Blacklist> findBlacklist = blacklistRepository.findByUserId(user.getId());

            // 블랙 리스트 회원일 경우
            if(findBlacklist.isPresent()) {
                log.debug("블랙리스트 사용자가 로그인을 시도했습니다.");
                throw new OAuth2AuthenticationException("접근 금지 사용자입니다.");
            }
            else {
                log.debug("기존 회원 로그인 성공: userId={}, role={}", user.getId(), user.getRole());
                userAttribute.put("exist", true);
                // 회원의 권한과, 회원속성, 속성이름을 이용해 DefaultOAuth2User 객체를 생성해 반환한다.
                return new DefaultOAuth2User(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_".concat(String.valueOf(findUser.get().getRole())))),
                        userAttribute, "socialId");
            }
        }
    }
}