package com.sparta.moa.post.service;

import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import com.sparta.moa.post.dto.PostCreateRequest;
import com.sparta.moa.post.dto.PostResponse;
import com.sparta.moa.post.entity.Post;
import com.sparta.moa.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    // Post 테이블에 게시글 저장하기 위해 DB 연결을 담당하는 Repository 필요
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    private static final Long TEMP_MEMBER_ID = 1L;

    // 게시글 등록
    @Transactional
    public PostResponse create(PostCreateRequest request) {

        // 멤버 조회 (게시글을 작성한 사람을 저장: member_id) : Optional<Member>
        Member member = memberRepository.findById(TEMP_MEMBER_ID).orElseThrow(
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
    public Page<PostResponse> findAll(Pageable pageable) {

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

        return postRepository.findAll(pageable).map(PostResponse::from);

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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return PostResponse.from(post);
    }

}
