package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Like;
import tn.esprit.pi.service.ILikeService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final ILikeService likeService;

    // Create Like
    @PostMapping
    public Like createLike(@RequestBody Like like) {
        return likeService.createLike(like);
    }

    // Get all Likes
    @GetMapping
    public List<Like> getAllLikes() {
        return likeService.getAllLikes();
    }

    // Get Like by id
    @GetMapping("/{id}")
    public Optional<Like> getLikeById(@PathVariable Long id) {
        return likeService.getLikeById(id);
    }

    // Delete Like
    @DeleteMapping("/{id}")
    public void deleteLike(@PathVariable Long id) {
        likeService.deleteLike(id);
    }

    // Get Likes by Post
    @GetMapping("/post/{postId}")
    public List<Like> getLikesByPost(@PathVariable Long postId) {
        return likeService.getLikesByPost(postId);
    }

    // Get Likes by User
    @GetMapping("/user/{userId}")
    public List<Like> getLikesByUser(@PathVariable Long userId) {
        return likeService.getLikesByUser(userId);
    }
}
