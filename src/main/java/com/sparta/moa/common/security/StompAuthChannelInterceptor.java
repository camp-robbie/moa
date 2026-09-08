package com.sparta.moa.common.security;

import com.sparta.moa.common.util.JwtUtil;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * 들어오는 STOMP 프레임을 가로챕니다.
 * CONNECT 한 번만 검사하고, 그 뒤로는 연결에 붙은 이름표를 씁니다.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // CONNECT 가 아니면 그냥 흘려보낸다. SUBSCRIBE 마다 다시 검사하지 않는다
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String header = accessor.getFirstNativeHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("STOMP CONNECT: 토큰이 없습니다");
        }

        Long memberId = jwtUtil.getMemberId(header.substring(7));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "STOMP CONNECT: 회원을 찾을 수 없습니다. id=" + memberId));

        MemberDetails details = new MemberDetails(member);

        // 이 이름표가 연결이 끊길 때까지 붙어 있는다.
        // getName() 이 이메일을 돌려주므로, 보낼 때도 이메일로 찾는다
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities()));

        return message;
    }
}