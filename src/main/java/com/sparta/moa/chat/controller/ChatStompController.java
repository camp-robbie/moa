package com.sparta.moa.chat.controller;

import com.sparta.moa.chat.dto.ChatMessageCreateRequest;
import com.sparta.moa.chat.service.ChatMessageService;
import com.sparta.moa.chat.service.SentMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 브라우저가 /app/chat/... 으로 보낸 STOMP 메시지를 받습니다.
 * @RestController 가 아니라 @Controller 입니다. 돌려줄 HTTP 응답이 없습니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;

    // 브라우저는 /app/chat/rooms/3/messages 로 보낸다. /app 은 떼고 적는다
    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void send(@DestinationVariable Long roomId,
                     @Payload @Valid ChatMessageCreateRequest request,
                     Principal principal) {

        // principal.getName() 은 이메일. 인터셉터가 붙여 둔 이름표다
        SentMessage sent = chatMessageService.save(roomId, principal.getName(), request.content());

        // 저장이 커밋된 뒤에 민다. 이 메서드에는 @Transactional 이 없다(3-5)
        chatMessageService.push(sent);
    }

    // SEND 에는 응답이 없다. 여기서 잡지 않으면 예외가 조용히 사라진다
    @MessageExceptionHandler
    public void handle(Exception e) {
        log.warn("쪽지 전송 실패", e);
    }
}