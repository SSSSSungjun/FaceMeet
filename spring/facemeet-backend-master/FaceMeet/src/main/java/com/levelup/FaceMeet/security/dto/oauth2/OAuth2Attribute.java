package com.levelup.FaceMeet.security.dto.oauth2;

import com.levelup.FaceMeet.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ToString
@Builder(access = AccessLevel.PRIVATE) // Builder 메서드를 외부에서 사용하지 않으므로, Private 제어자로 지정
@Getter
public class OAuth2Attribute {
    private Map<String, Object> attributes; // 사용자 속성 정보를 담는 Map
    private Long userId;
    private String socialId; // 사용자 속성의 키 값, 소셜 id
    private String email;
    private String name;
    private String gender;
    private String birthday;
    private String birthyear;
    private String provider; // 제공자 정보 (kakao, naver)

    // 서비스에 따라 OAuth2Attribute 객체를 생성하는 메서드
    public static OAuth2Attribute of(String provider, String attributeKey,
                                     Map<String, Object> attributes) {
        switch (provider) {
            case "kakao":
                return ofKakao(provider, attributeKey, attributes);
            case "naver":
                return ofNaver(provider, attributeKey, attributes);
            default:
                throw new RuntimeException("지원하지 않는 소셜 로그인: " + provider);
        }
    }

    /*
     *   Kakao 로그인일 경우 사용하는 메서드, 필요한 사용자 정보가 kakaoAccount -> kakaoProfile 두번 감싸져 있어서,
     *   두번 get() 메서드를 이용해 사용자 정보를 담고있는 Map을 꺼내야한다.
     * */
    private static OAuth2Attribute ofKakao(String provider, String attributeKey,
                                           Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("kakao_account");

        String socialId = attributes.get(attributeKey).toString();

        return OAuth2Attribute.builder()
                .socialId(socialId)
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .gender((String) response.get("gender"))
                .birthday((String) response.get("birthday"))
                .birthyear((String) response.get("birthyear"))
                .attributes(response)
                .provider(provider)
                .build();
    }

    /*
     *  Naver 로그인일 경우 사용하는 메서드, 필요한 사용자 정보가 response Map에 감싸져 있어서,
     *  한번 get() 메서드를 이용해 사용자 정보를 담고있는 Map을 꺼내야한다.
     * */
    private static OAuth2Attribute ofNaver(String provider, String attributeKey,
                                           Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attribute.builder()
                .socialId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .gender((String) response.get("gender"))
                .birthday((String) response.get("birthday"))
                .birthyear((String) response.get("birthyear"))
                .attributes(response)
                .provider(provider)
                .build();
    }


    // OAuth2User 객체에 넣어주기 위해서 Map으로 값들을 반환해준다.
    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("socialId", socialId);
        map.put("email", email);
        map.put("name", name);
        map.put("gender", gender);
        map.put("birthday", birthday);
        map.put("birthyear", birthyear);
        map.put("provider", provider);

        return map;
    }

    public User toUserEntity() {
        return User.builder()
                .socialId(this.socialId)
                .email(this.email)
                .name(this.name)
                .gender(parseGender(this.gender))
                .birth(parseBirthDate(this.birthyear, this.birthday))
                .build();
    }

    private User.Gender parseGender(String genderStr) {
        if ("F".equalsIgnoreCase(genderStr)) return User.Gender.f;
        else return User.Gender.m;
    }

    private LocalDateTime parseBirthDate(String year, String monthDay) {
        if (year == null || monthDay == null) return null;
        String dateStr = year + "-" + monthDay; // ex: "1990-07-21"
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
    }
}