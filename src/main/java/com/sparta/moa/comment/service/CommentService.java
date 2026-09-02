package com.sparta.moa.comment.service;

import com.sparta.moa.comment.dto.CommentCreateRequest;
import com.sparta.moa.comment.dto.CommentResponse;
import com.sparta.moa.comment.dto.CommentUpdateRequest;
import com.sparta.moa.comment.entity.Comment;
import com.sparta.moa.comment.repository.CommentRepository;
import com.sparta.moa.common.dto.CursorResponse;
import com.sparta.moa.common.exception.ForbiddenException;
import com.sparta.moa.common.exception.NotFoundException;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import com.sparta.moa.post.entity.Post;
import com.sparta.moa.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @CacheEvict(value = "postList", allEntries = true)
    public CommentResponse create(Long postId, Long memberId, CommentCreateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + memberId));

        Comment comment = commentRepository.save(
                new Comment(post, member, request.content()));

        return CommentResponse.from(comment);
    }

    public CursorResponse<CommentResponse> findByPost(Long postId, Long cursor, int size) {

        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId);
        }

        return commentRepository.findByCursor(postId, cursor, size);
    }

    @Transactional
    public CommentResponse update(Long postId, Long commentId, Long memberId,
                                  CommentUpdateRequest request) {

        Comment comment = findCommentOrThrow(postId, commentId);

        if (!comment.getMember().getId().equals(memberId)) {
            throw new ForbiddenException("본인이 작성한 댓글만 수정할 수 있습니다");
        }

        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    @CacheEvict(value = "postList", allEntries = true)
    public void delete(Long postId, Long commentId, Long memberId) {

        Comment comment = findCommentOrThrow(postId, commentId);

        if (!comment.getMember().getId().equals(memberId)) {
            throw new ForbiddenException("본인이 작성한 댓글만 삭제할 수 있습니다");
        }

        commentRepository.delete(comment);
    }

    // 댓글을 찾고, 그 댓글이 정말 이 글의 댓글인지까지 확인한다
    private Comment findCommentOrThrow(Long postId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "댓글을 찾을 수 없습니다. id=" + commentId));

        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("댓글을 찾을 수 없습니다. id=" + commentId);
        }
        return comment;
    }
}