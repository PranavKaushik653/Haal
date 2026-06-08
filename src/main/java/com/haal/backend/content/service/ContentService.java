package com.haal.backend.content.service;

import com.haal.backend.auth.repository.UserRepository;
import com.haal.backend.content.entity.*;
import com.haal.backend.content.dto.CreatePostRequest;
import com.haal.backend.content.dto.PostResponse;
import com.haal.backend.content.repository.PostLikeRepository;
import com.haal.backend.content.repository.PostRepository;
import com.haal.backend.shared.exception.ErrorCode;
import com.haal.backend.shared.exception.HaalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService implements ContentServicePort {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    private static final int SRID = 4326;
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), SRID);

    @Override
    @Transactional
    public PostResponse createPost(UUID userId, CreatePostRequest request) {
        // Validate user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        Point location = buildPoint(request.getLatitude(), request.getLongitude());

        Post post = Post.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .visibility(request.getVisibility() != null
                        ? request.getVisibility()
                        : PostVisibility.PUBLIC)
                .location(location)
                .moderationStatus(ModerationStatus.PENDING)
                .build();

        post = postRepository.save(post);
        log.info("Post created: id={}, author={}", post.getId(), user.getAnonymousAlias());

        return toResponse(post, user.getAnonymousAlias(), userId, 0.0, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(UUID userId, Pageable pageable) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return posts.map(p -> toResponse(p, user.getAnonymousAlias(), userId, 0.0,
                postLikeRepository.existsByPostIdAndUserId(p.getId(), userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPublicFeed(Pageable pageable) {
        Page<Post> posts = postRepository.findByVisibilityAndModerationStatusOrderByCreatedAtDesc(
                PostVisibility.PUBLIC, ModerationStatus.APPROVED, pageable);

        return posts.map(p -> {
            var author = userRepository.findById(p.getUserId()).orElse(null);
            return toResponse(p, author != null ? author.getAnonymousAlias() : "anonymous",
                    null, 0.0, false);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getNearbyPosts(UUID userId, Double latitude,
                                             Double longitude, Pageable pageable) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        Point userLocation = buildPoint(latitude, longitude);
        if (userLocation == null) {
            // Fall back to public feed if no location
            return getPublicFeed(pageable);
        }

        Page<Post> posts = postRepository.findNearbyPosts(userLocation, pageable);

        return posts.map(p -> {
            var author = userRepository.findById(p.getUserId()).orElse(null);
            double distanceKm = calculateDistance(userLocation, p.getLocation());
            return toResponse(p, author != null ? author.getAnonymousAlias() : "anonymous",
                    userId, distanceKm,
                    postLikeRepository.existsByPostIdAndUserId(p.getId(), userId));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId, UUID requestingUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        // Private posts only visible to author
        if (post.getVisibility() == PostVisibility.PRIVATE &&
                !post.getUserId().equals(requestingUserId)) {
            throw HaalException.forbidden();
        }

        var author = userRepository.findById(post.getUserId()).orElse(null);
        boolean liked = requestingUserId != null &&
                postLikeRepository.existsByPostIdAndUserId(postId, requestingUserId);

        return toResponse(post, author != null ? author.getAnonymousAlias() : "anonymous",
                requestingUserId, 0.0, liked);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findByIdAndUserId(postId, userId)
                .orElseThrow(() -> HaalException.forbidden());

        postRepository.delete(post);
        log.info("Post deleted: id={}", postId);
    }

    @Override
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        postRepository.findById(postId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        // Idempotent: ignore if already liked
        if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postLikeRepository.save(like);

            // Increment post likes
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                post.setLikesCount(post.getLikesCount() + 1);
                postRepository.save(post);
            }
        }
    }

    @Override
    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        postLikeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> {
                    postLikeRepository.delete(like);

                    // Decrement post likes
                    Post post = postRepository.findById(postId).orElse(null);
                    if (post != null && post.getLikesCount() > 0) {
                        post.setLikesCount(post.getLikesCount() - 1);
                        postRepository.save(post);
                    }
                });
    }

    @Override
    @Transactional
    public void approvePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        post.setModerationStatus(ModerationStatus.APPROVED);
        postRepository.save(post);
        log.info("Post approved: id={}", postId);
    }

    @Override
    @Transactional
    public void rejectPost(UUID postId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        post.setModerationStatus(ModerationStatus.REJECTED);
        post.setModerationReason(reason);
        postRepository.save(post);
        log.info("Post rejected: id={}, reason={}", postId, reason);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private PostResponse toResponse(Post post, String authorAlias, UUID userId,
                                    double distanceKm, boolean likedByCurrentUser) {
        return PostResponse.builder()
                .id(post.getId())
                .anonymousAuthorAlias(authorAlias)
                .title(post.getTitle())
                .content(post.getContent())
                .visibility(post.getVisibility())
                .distanceKm(distanceKm > 0 ? distanceKm : null)
                .likesCount((long) post.getLikesCount())
                .likedByCurrentUser(likedByCurrentUser)
                .createdAt(post.getCreatedAt())
                .moderationStatus(post.getModerationStatus())
                .build();
    }

    private Point buildPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private double calculateDistance(Point p1, Point p2) {
        if (p1 == null || p2 == null) return 0.0;
        // Haversine distance in km
        return p1.distance(p2) * 111.0;  // Rough conversion degrees to km
    }
}