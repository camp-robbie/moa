package com.sparta.moa.chat.service;

import com.sparta.moa.chat.dto.ChatRoomResponse;
import com.sparta.moa.chat.entity.ChatRoom;
import com.sparta.moa.chat.repository.ChatRoomRepository;
import com.sparta.moa.common.exception.ConflictException;
import com.sparta.moa.common.exception.NotFoundException;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoomResponse createOrGet(Long memberId, String partnerNickname) {

        Member me = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + memberId));

        Member partner = memberRepository.findFirstByNickname(partnerNickname)
                .orElseThrow(() -> new NotFoundException(
                        "회원을 찾을 수 없습니다. nickname=" + partnerNickname));

        if (partner.getId().equals(memberId)) {
            throw new ConflictException("자기 자신에게는 쪽지를 보낼 수 없습니다");
        }

        Long lowId = Math.min(me.getId(), partner.getId());
        Long highId = Math.max(me.getId(), partner.getId());

        ChatRoom room = chatRoomRepository
                .findByMemberLowIdAndMemberHighId(lowId, highId)
                .orElseGet(() -> saveOrFindAgain(me, partner, lowId, highId));

        return ChatRoomResponse.from(room, null, memberId);
    }

    // 검사와 저장 사이에 상대가 먼저 방을 열었을 수 있다.
    // 제약에 걸렸다고 실패가 아니다. 이미 생긴 그 방을 쓰면 된다
    private ChatRoom saveOrFindAgain(Member me, Member partner, Long lowId, Long highId) {
        try {
            return chatRoomRepository.save(new ChatRoom(me, partner));
        } catch (DataIntegrityViolationException e) {
            return chatRoomRepository.findByMemberLowIdAndMemberHighId(lowId, highId)
                    .orElseThrow(() -> new NotFoundException("방을 만들지 못했습니다"));
        }
    }

    public List<ChatRoomResponse> findMyRooms(Long memberId) {
        return chatRoomRepository.findMyRooms(memberId).stream()
                .map(r -> ChatRoomResponse.from(r.room(), r.last(), memberId))
                .toList();
    }
}