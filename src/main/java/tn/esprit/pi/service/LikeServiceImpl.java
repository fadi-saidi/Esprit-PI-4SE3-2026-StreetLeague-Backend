package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements ILikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Like createLike(Long postId, Long userId, LikeType likeType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            // UPDATE
            Like like = existingLike.get();
            like.setLikeType(likeType);
            return likeRepository.save(like);
        }

        // CREATE
        Like like = new Like();
        like.setLikeType(likeType);
        like.setPost(post);
        like.setUser(user);

        return likeRepository.save(like);
    }

    @Override
    public List<Like> getAllLikes() {
        return likeRepository.findAll();
    }

    @Override
    public Optional<Like> getLikeById(Long id) {
        return likeRepository.findById(id);
    }

    @Override
    public void deleteLike(Long id) {
        // ✅ SIMPLE ET PROPRE
        likeRepository.deleteById(id);
        // OU
        // Like like = likeRepository.findById(id)
        //         .orElseThrow(() -> new RuntimeException("Like not found: " + id));
        // likeRepository.delete(like);
    }

    @Override
    public List<Like> getLikesByPost(Long postId) {
        return likeRepository.findByPostId(postId);
    }

    @Override
    public List<Like> getLikesByUser(Long userId) {
        return likeRepository.findByUserId(userId);
    }
}