package tn.esprit.pi.service;

import tn.esprit.pi.dto.PostDto;
import java.util.List;
import java.util.Optional;

public interface IPostService {
        PostDto createPost(Long userId, String content);
        List<PostDto> getAllPosts();
        Optional<PostDto> getPostById(Long id);
        PostDto updatePost(Long id, String newContent);
        void deletePost(Long id);
        List<PostDto> getPostsByUserId(Long userId);

        // Moderation
        List<PostDto> getFlaggedPosts();
        PostDto approvePost(Long id);
        void rejectPost(Long id);
}