package com.sparta.moa.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "이메일은 비어 있을 수 없습니다")
        @Email(message = "이메일 형식이 아닙니다")
        @Size(max = 255, message = "이메일은 255자를 넘을 수 없습니다")
        String email,

        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        String password,

        @NotBlank(message = "닉네임은 비어 있을 수 없습니다")
        @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하입니다")
        String nickname
) {}