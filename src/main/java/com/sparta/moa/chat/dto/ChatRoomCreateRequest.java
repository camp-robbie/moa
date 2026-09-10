package com.sparta.moa.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRoomCreateRequest(
        @NotBlank(message = "상대를 지정해 주세요")
        String partner
) {
}