package com.sparta.moa.member.service;

import com.sparta.moa.common.exception.DuplicateEmailException;
import com.sparta.moa.common.exception.LoginFailedException;
import com.sparta.moa.common.util.JwtUtil;
import com.sparta.moa.member.dto.LoginRequest;
import com.sparta.moa.member.dto.LoginResponse;
import com.sparta.moa.member.dto.MemberResponse;
import com.sparta.moa.member.dto.SignupRequest;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public MemberResponse signup(SignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
        }

        Member member = memberRepository.save(new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        ));

        return MemberResponse.from(member);
    }

    public LoginResponse login(LoginRequest request) {

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new LoginFailedException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new LoginFailedException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        return new LoginResponse(
                jwtUtil.createToken(member.getId()),
                MemberResponse.from(member)
        );
    }
}
