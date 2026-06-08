package com.haal.backend.content.service;


import com.haal.backend.content.dto.CreatePostRequest;
import com.haal.backend.content.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContentServicePort {
    PostResponse createPost(UUID userId, CreatePostRequest request);

    Page<PostResponse> getMyPosts(UUID userId, Pageable pageable);

    Page<PostResponse> getPublicFeed(Pageable pageable);

    Page<PostResponse> getNearbyPosts(UUID userId, Double latitude,
                                      Double longitude, Pageable pageable);

    PostResponse getPost(UUID postId, UUID requestingUserId);

    void deletePost(UUID postId, UUID userId);

    void likePost(UUID postId, UUID userId);

    void unlikePost(UUID postId, UUID userId);

    void approvePost(UUID postId);

    void rejectPost(UUID postId, String reason);

}
