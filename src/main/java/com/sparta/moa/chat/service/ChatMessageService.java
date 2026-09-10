package com.sparta.moa.chat.service;

import com.sparta.moa.chat.dto.ChatMessagePush;
import com.sparta.moa.chat.dto.ChatMessageResponse;
import com.sparta.moa.chat.entity.ChatMessage;
import com.sparta.moa.chat.entity.ChatRoom;
import com.sparta.moa.chat.repository.ChatMessageRepository;
import com.sparta.moa.chat.repository.ChatRoomRepository;
import com.sparta.moa.common.dto.CursorResponse;
import com.sparta.moa.common.exception.ForbiddenException;
import com.sparta.moa.common.exception.NotFoundException;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 저장까지만. 밀기는 이 트랜잭션 안에서 하지 않는다(3-5)
    @Transactional
    public SentMessage save(Long roomId, String senderEmail, String content) {

        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new NotFoundException(
                        "회원을 찾을 수 없습니다. email=" + senderEmail));

        ChatRoom room = findRoomOrThrow(roomId, sender.getId());

        chatMessageRepository.save(new ChatMessage(room, sender, content));

        // 트랜잭션 밖에서는 프록시를 못 연다. 필요한 값을 여기서 다 꺼내 둔다
        Member partner = room.partnerOf(sender.getId());

        return new SentMessage(
                sender.getEmail(),
                partner.getEmail(),
                new ChatMessagePush(roomId, sender.getNickname()));
    }

    // 커밋된 뒤에 불린다. 상대에게 — 그리고 보낸 사람에게도(에코)
    public void push(SentMessage sent) {
        pushTo(sent.partnerEmail(), sent.push());
        pushTo(sent.senderEmail(), sent.push());
    }

    private void pushTo(String email, ChatMessagePush push) {
        try {
            messagingTemplate.convertAndSendToUser(email, "/queue/messages", push);
        } catch (Exception e) {
            log.warn("쪽지 알림 전송 실패. email={}", email, e);
        }
    }

    public CursorResponse<ChatMessageResponse> findByCursor(
            Long roomId, Long memberId, Long cursor, int size) {

        findRoomOrThrow(roomId, memberId);
        return chatMessageRepository.findByCursor(roomId, cursor, size, memberId);
    }

    // 방이 있는지, 그리고 내가 그 방 사람인지까지 확인한다
    private ChatRoom findRoomOrThrow(Long roomId, Long memberId) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다. id=" + roomId));

        if (!room.has(memberId)) {
            throw new ForbiddenException("이 대화에 참여한 사람만 볼 수 있습니다");
        }
        return room;
    }
}