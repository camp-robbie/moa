package com.sparta.moa.member.dto;

public record LoginResponse(
        String accessToken,
        MemberResponse member
) {}
