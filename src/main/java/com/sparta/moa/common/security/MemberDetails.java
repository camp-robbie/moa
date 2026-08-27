package com.sparta.moa.common.security;

import com.sparta.moa.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class MemberDetails implements UserDetails {

    private final Member member;

    // Controller 에서 @AuthenticationPrincipal 로 꺼내 쓸 값
    public Long getMemberId() {
        return member.getId();
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 이 프로젝트에는 권한 구분이 없습니다
        return List.of();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }
}
