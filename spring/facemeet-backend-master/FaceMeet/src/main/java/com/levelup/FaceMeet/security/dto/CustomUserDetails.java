package com.levelup.FaceMeet.security.dto;

import com.levelup.FaceMeet.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
public class CustomUserDetails implements UserDetails {

    private Long userId;
    private String role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() { return ""; }

    public static CustomUserDetails from(User user) {
        return CustomUserDetails.builder()
                .userId(user.getId())
                .role("ROLE_" + user.getRole())
                .build();
    }
}
