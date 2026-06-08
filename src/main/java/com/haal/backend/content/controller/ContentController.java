package com.haal.backend.content.controller;

import com.haal.backend.auth.security.HaalUserDetails;
import com.haal.backend.content.dto.CreatePostRequest;
import com.haal.backend.content.dto.PostResponse;
import com.haal.backend.content.service.ContentServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "Posts, feed, and interactions")
public class ContentController {

    private final ContentServicePort contentService;

    @PostMapping
    @Operation(summary = "Create anonymous post (public or private)")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentService.createPost(userDetails.getUserId(), request));
    }

    @GetMapping("/my-posts")
    @Operation(summary = "Get current user's posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contentService.getMyPosts(
                userDetails.getUserId(),
                PageRequest.of(page, size)));
    }

    @GetMapping("/feed")
    @Operation(summary = "Get global public feed")
    public ResponseEntity<Page<PostResponse>> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contentService.getPublicFeed(
                PageRequest.of(page, size)));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get posts from nearby users (10km radius)")
    public ResponseEntity<Page<PostResponse>> getNearbyPosts(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contentService.getNearbyPosts(
                userDetails.getUserId(), latitude, longitude,
                PageRequest.of(page, size)));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a single post")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String postId,
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        return ResponseEntity.ok(contentService.getPost(
                java.util.UUID.fromString(postId),
                userDetails.getUserId()));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete own post")
    public ResponseEntity<Void> deletePost(
            @PathVariable String postId,
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        contentService.deletePost(java.util.UUID.fromString(postId),
                userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<Void> likePost(
            @PathVariable String postId,
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        contentService.likePost(java.util.UUID.fromString(postId),
                userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<Void> unlikePost(
            @PathVariable String postId,
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        contentService.unlikePost(java.util.UUID.fromString(postId),
                userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/approve")
    @Operation(summary = "Approve post (admin only)")
    public ResponseEntity<Void> approvePost(@PathVariable String postId) {
        contentService.approvePost(java.util.UUID.fromString(postId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/reject")
    @Operation(summary = "Reject post (admin only)")
    public ResponseEntity<Void> rejectPost(
            @PathVariable String postId,
            @RequestParam String reason) {
        contentService.rejectPost(java.util.UUID.fromString(postId), reason);
        return ResponseEntity.noContent().build();
    }
}