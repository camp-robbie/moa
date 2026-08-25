package com.sparta.moa.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "제목은 비어 있을 수 없습니다")
        @Size(max = 200, message = "제목은 200자를 넘을 수 없습니다")
        String title,

        @NotBlank(message = "내용은 비어 있을 수 없습니다")
        String content
) {
}
