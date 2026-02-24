package tn.esprit.pi.service;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.service.IPostService;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PostServiceimpl implements IPostService {



        private final PostRepository postRepository;

        @Override
        public Post createPost(Post post) {
            return postRepository.save(post);
        }

        @Override
        public List<Post> getAllPosts() {
            return postRepository.findAll();
        }

        @Override
        public Optional<Post> getPostById(Long id) {
            return postRepository.findById(id);
        }

        @Override
        public Post updatePost(Long id, Post post) {
            return postRepository.findById(id).map(existingPost -> {
                existingPost.setContent(post.getContent());
                existingPost.setCreationDate(post.getCreationDate());
                existingPost.setUser(post.getUser());
                return postRepository.save(existingPost);
            }).orElseThrow(() -> new RuntimeException("Post not found with id " + id));
        }

        @Override
        public void deletePost(Long id) {
            postRepository.deleteById(id);
        }

        @Override
        public List<Post> getPostsByUserId(Long userId) {
            return postRepository.findByUserId(userId);
        }

}
