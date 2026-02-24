package tn.esprit.pi.service;
import tn.esprit.pi.domain.Post;

import java.util.List;
import java.util.Optional;

public interface IPostService {


        Post createPost(Post post);
        List<Post> getAllPosts();
        Optional<Post> getPostById(Long id);
        Post updatePost(Long id, Post post);
        void deletePost(Long id);
        List<Post> getPostsByUserId(Long userId);
    }

