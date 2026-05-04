package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Like;
import tn.esprit.pi.domain.LikeType;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.LikeDto;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.ILikeService;
import tn.esprit.pi.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final ILikeService likeService;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // POST /likes?postId=1&userId=2&likeType=LOVE
    @PostMapping
    public ResponseEntity<LikeDto> createLike(@RequestParam Long postId,
                                              @RequestParam Long userId,
                                              @RequestParam LikeType likeType) {

        Like savedLike = likeService.createLike(postId, userId, likeType);

        // Notify post owner — use fresh DB lookups, never navigate lazy fields
        try {
            Post post = postRepository.findById(postId).orElse(null);
            User liker = userRepository.findById(userId).orElse(null);
            System.out.println("[NOTIF] post=" + (post != null ? post.getId() : "NULL")
                    + " postOwner=" + (post != null && post.getUser() != null ? post.getUser().getId() : "NULL")
                    + " liker=" + (liker != null ? liker.getId() : "NULL")
                    + " sameUser=" + (post != null && post.getUser() != null && post.getUser().getId().equals(userId)));
            if (post != null && post.getUser() != null
                    && liker != null
                    && !post.getUser().getId().equals(userId)) {
                notificationService.notifyPostLiked(post.getUser(), liker.getUsername(), postId);
                System.out.println("[NOTIF] saved like-notification for userId=" + post.getUser().getId());
            }
        } catch (Exception e) {
            System.err.println("[LikeController] notification failed: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(toDto(savedLike));
    }

    // GET /likes
    @GetMapping
    public ResponseEntity<List<LikeDto>> getAllLikes() {
        return ResponseEntity.ok(
                likeService.getAllLikes()
                        .stream()
                        .map(this::toDto)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    // GET /likes/1
    @GetMapping("/{id}")
    public ResponseEntity<LikeDto> getLikeById(@PathVariable Long id) {
        return likeService.getLikeById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /likes/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLike(@PathVariable Long id) {
        likeService.deleteLike(id);
        return ResponseEntity.noContent().build();
    }

    // GET /likes/post/1
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<LikeDto>> getLikesByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(
                likeService.getLikesByPost(postId)
                        .stream()
                        .map(this::toDto)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    // GET /likes/user/1
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LikeDto>> getLikesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                likeService.getLikesByUser(userId)
                        .stream()
                        .map(this::toDto)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    private LikeDto toDto(Like like) {
        LikeDto dto = new LikeDto();
        dto.setId(like.getId());
        dto.setPostId(like.getPost().getId());
        dto.setUserId(like.getUser().getId());
        dto.setLikeType(like.getLikeType());
        dto.setCreationDate(like.getCreationDate());
        return dto;
    }
}
