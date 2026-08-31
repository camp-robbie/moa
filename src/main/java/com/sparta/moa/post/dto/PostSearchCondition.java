package com.sparta.moa.post.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PostSearchCondition(

        String title,
        String nickname,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // ?from=2026-08-01 => LocalDate 형식을 변환해주는 어노테이션
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {}