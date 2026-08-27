package com.sparta.moa.member.controller;

import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.member.dto.LoginRequest;
import com.sparta.moa.member.dto.LoginResponse;
import com.sparta.moa.member.dto.MemberResponse;
import com.sparta.moa.member.dto.SignupRequest;
import com.sparta.moa.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(
            @Valid @RequestBody SignupRequest request) {

        MemberResponse response = memberService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, memberService.login(request)));
    }
}
