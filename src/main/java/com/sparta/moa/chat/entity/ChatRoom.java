package com.sparta.moa.chat.entity;

import com.sparta.moa.common.entity.BaseEntity;
import com.sparta.moa.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_room", uniqueConstraints = @UniqueConstraint(
        name = "uk_chat_room_members",
        columnNames = {"member_low_id", "member_high_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_low_id", nullable = false)
    private Member memberLow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_high_id", nullable = false)
    private Member memberHigh;

    // 누가 먼저 열든 id 가 작은 쪽이 low 로 간다. (A,B) 와 (B,A) 가 같은 줄이 된다
    public ChatRoom(Member a, Member b) {
        if (a.getId().compareTo(b.getId()) < 0) {
            this.memberLow = a;
            this.memberHigh = b;
        } else {
            this.memberLow = b;
            this.memberHigh = a;
        }
    }

    public Member partnerOf(Long memberId) {
        return memberLow.getId().equals(memberId) ? memberHigh : memberLow;
    }

    public boolean has(Long memberId) {
        return memberLow.getId().equals(memberId) || memberHigh.getId().equals(memberId);
    }
}