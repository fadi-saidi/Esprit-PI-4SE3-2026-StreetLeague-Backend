package tn.esprit.pi.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.service.IPostService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {


        private final IPostService postService;

        // Create a new post
        @PostMapping
        public Post createPost(@RequestBody Post post) {
            return postService.createPost(post);
        }

        // Get all posts
        @GetMapping
        public List<Post> getAllPosts() {
            return postService.getAllPosts();
        }

        // Get post by id
        @GetMapping("/{id}")
        public Optional<Post> getPostById(@PathVariable Long id) {
            return postService.getPostById(id);
        }

        // Update post
        @PutMapping("/{id}")
        public Post updatePost(@PathVariable Long id, @RequestBody Post post) {
            return postService.updatePost(id, post);
        }

        // Delete post
        @DeleteMapping("/{id}")
        public void deletePost(@PathVariable Long id) {
            postService.deletePost(id);
        }

        // Get posts by user
        @GetMapping("/user/{userId}")
        public List<Post> getPostsByUser(@PathVariable Long userId) {
            return postService.getPostsByUserId(userId);
        }
    }

