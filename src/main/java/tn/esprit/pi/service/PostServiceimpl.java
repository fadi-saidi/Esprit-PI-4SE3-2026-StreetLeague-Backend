package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.PostDto;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceimpl implements IPostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BadWordsService badWordsService;

    @Override
    public PostDto createPost(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Post post = new Post();
        post.setContent(content);

        if (badWordsService.containsBadWords(content)) {
            post.setFlagged(true);
            post.setFlagReason("bad_words");
        }

        user.addPost(post);
        return toDto(postRepository.save(post));
    }

    @Override
    public List<PostDto> getAllPosts() {
        // Public feed: hide flagged posts (pending admin review)
        return postRepository.findAll()
                .stream()
                .filter(p -> !p.isFlagged())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PostDto> getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::toDto);
    }

    @Override
    public PostDto updatePost(Long id, String newContent) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
        existing.setContent(newContent);
        if (badWordsService.containsBadWords(newContent)) {
            existing.setFlagged(true);
            existing.setFlagReason("bad_words");
        } else {
            existing.setFlagged(false);
            existing.setFlagReason(null);
        }
        return toDto(postRepository.save(existing));
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    public List<PostDto> getPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Moderation ────────────────────────────────────────────────────────────
    @Override
    public List<PostDto> getFlaggedPosts() {
        return postRepository.findByFlaggedTrue()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PostDto approvePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
        post.setFlagged(false);
        post.setFlagReason(null);
        return toDto(postRepository.save(post));
    }

    @Override
    public void rejectPost(Long id) {
        postRepository.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private PostDto toDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreationDate(post.getCreationDate());
        dto.setUserId(post.getUser().getId());
        dto.setUsername(post.getUser().getUsername());
        dto.setFlagged(post.isFlagged());
        dto.setFlagReason(post.getFlagReason());
        return dto;
    }
}