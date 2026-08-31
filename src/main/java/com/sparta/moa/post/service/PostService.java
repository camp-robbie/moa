package com.sparta.moa.post.service;

import com.sparta.moa.common.dto.PageResponse;
import com.sparta.moa.common.exception.ForbiddenException;
import com.sparta.moa.common.exception.NotFoundException;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import com.sparta.moa.post.dto.PostCreateRequest;
import com.sparta.moa.post.dto.PostResponse;
import com.sparta.moa.post.dto.PostSearchCondition;
import com.sparta.moa.post.dto.PostUpdateRequest;
import com.sparta.moa.post.entity.Post;
import com.sparta.moa.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    // Post 테이블에 게시글 저장하기 위해 DB 연결을 담당하는 Repository 필요
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    
    // 게시글 등록
    @Transactional
    public PostResponse create(Long memberId, PostCreateRequest request) {

        // 멤버 조회 (게시글을 작성한 사람을 저장: member_id) : Optional<Member>
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        // 게시글을 생성 (Post Entity new)
        Post post = new Post(
                member,
                request.title(),
                request.content()
        ); // 게시글 하나 생성, 테이블에서는 한 줄

        // 게시글을 저장 (insert)
        postRepository.save(post); // insert

        // 게시글을 변환 (entity -> DTO)
        // 게시글 정보를 가진 DTO 반환
        return PostResponse.from(post);
    }

    // 게시글 목록 조회 : 페이징 (10개씩)
    public PageResponse<PostResponse> findAll(Pageable pageable) {

        /** 페이징
         *
         * 우리가 클라이언트에 페이징 처리에 대한 결과물
         * content : 게시글 목록
         * totalElement : 전체 게시글 개수
         * totalPage : 전체 페이지 개수
         * currentPage : 현제 페이지 넘버
         * 사용자가 화면에서 페이징 정보를 볼 때, 전체 페이지 수가 몇개인지?
         *
         * */
        /**
        Page<Post> postPage = postRepository.findAll(pageable); // select

        // Page -> Stream . map post -> postResponse
        // Page<Post> -> Page<PostResponse>
        Page<PostResponse> postResponsePage = postPage.map(PostResponse::from);
        return postResponsePage;
        **/

        return PageResponse.from(
                postRepository.findAll(pageable).map(PostResponse::from)
        );

        /** v2
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .toList();
         /**

         /** v1
        // 반환할 게시글 목록 DTO List
        List<PostResponse> postResponseList = new ArrayList<>();

        // 게시글 목록 조회 : List<Post>
        List<Post> postList = postRepository.findAll();

        // List<Post> -> List<PostResponse>
        for (Post post : postList) {
            // Post -> PostResponse
            PostResponse postResponse = PostResponse.from(post);

            // List<PostResponse>에 추가
            postResponseList.add(postResponse);
        }

        return postResponseList;
        */
    }

    public PostResponse findOne(Long postId) {
        Post post = findPostOrThrow(postId);

        return PostResponse.from(post);
    }

    public PageResponse<PostResponse> search(PostSearchCondition condition, Pageable pageable) {
        return PageResponse.from(
                postRepository.search(condition, pageable).map(PostResponse::from)
        );
    }

    @Transactional
    public PostResponse update(Long postId, Long memberId, PostUpdateRequest request) {
        
        // 소유권 SQL : select * from post where id = 2 and member_id = 1; => 해당 게시글의 소유자가 아니면 조회가 안됨!
        Post post = findPostOrThrow(postId);

        validateOwner(post, memberId);

        post.update(request.title(), request.content());

        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long postId, Long memberId) {
        Post post = findPostOrThrow(postId);

        validateOwner(post, memberId);
        postRepository.delete(post);
    }

    private void validateOwner(Post post, Long memberId) {
        if(!post.getMember().getId().equals(memberId)) { // 해당 게시글의 작성자 id == 토큰에 꺼내온 로그인한 회원 id
            throw new ForbiddenException("해당 게시글의 소유자만 수정 및 삭제가 가능합니다.");
        }
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id = " + postId));
    }

}
