package com.sparta.moa.comment.repository;

import com.sparta.moa.comment.dto.CommentResponse;
import com.sparta.moa.common.dto.CursorResponse;

public interface CommentQueryRepository {

    CursorResponse<CommentResponse> findByCursor(Long postId, Long cursor, int size);
}
