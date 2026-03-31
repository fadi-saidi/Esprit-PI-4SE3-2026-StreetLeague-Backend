package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.PostDto;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.PostServiceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @InjectMocks
    private PostServiceimpl service;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("feryel");

        post = new Post();
        post.setId(10L);
        post.setContent("Hello community!");
        post.setCreationDate(LocalDateTime.of(2026, 3, 31, 10, 0));
        post.setUser(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createPost
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldCreatePost() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDto result = service.createPost(1L, "Hello community!");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Hello community!", result.getContent());
        assertEquals(1L, result.getUserId());
        assertEquals("feryel", result.getUsername());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void shouldThrowWhenUserNotFoundOnCreate() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createPost(99L, "some content"));

        assertTrue(ex.getMessage().contains("99"));
        verify(postRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllPosts
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnAllPosts() {
        Post second = new Post();
        second.setId(20L);
        second.setContent("Second post");
        second.setCreationDate(LocalDateTime.now());
        second.setUser(user);

        when(postRepository.findAll()).thenReturn(List.of(post, second));

        List<PostDto> results = service.getAllPosts();

        assertEquals(2, results.size());
        assertEquals(10L, results.get(0).getId());
        assertEquals(20L, results.get(1).getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoPostsExist() {
        when(postRepository.findAll()).thenReturn(List.of());

        List<PostDto> results = service.getAllPosts();

        assertTrue(results.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPostById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnPostDtoWhenPostExists() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        Optional<PostDto> result = service.getPostById(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
        assertEquals("Hello community!", result.get().getContent());
        assertEquals("feryel", result.get().getUsername());
    }

    @Test
    void shouldReturnEmptyOptionalWhenPostNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PostDto> result = service.getPostById(99L);

        assertFalse(result.isPresent());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updatePost
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldUpdatePostContent() {
        Post updated = new Post();
        updated.setId(10L);
        updated.setContent("Updated content");
        updated.setCreationDate(post.getCreationDate());
        updated.setUser(user);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(updated);

        PostDto result = service.updatePost(10L, "Updated content");

        assertEquals("Updated content", result.getContent());
        assertEquals(10L, result.getId());
        verify(postRepository).save(post);
    }

    @Test
    void shouldMutateContentBeforeSaveOnUpdate() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostDto result = service.updatePost(10L, "Mutated");

        assertEquals("Mutated", result.getContent());
    }

    @Test
    void shouldThrowWhenPostNotFoundOnUpdate() {
        when(postRepository.findById(55L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updatePost(55L, "anything"));

        assertTrue(ex.getMessage().contains("55"));
        verify(postRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deletePost
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldDeletePostById() {
        doNothing().when(postRepository).deleteById(10L);

        service.deletePost(10L);

        verify(postRepository).deleteById(10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPostsByUserId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnPostsForGivenUser() {
        when(postRepository.findByUserId(1L)).thenReturn(List.of(post));

        List<PostDto> results = service.getPostsByUserId(1L);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getUserId());
        assertEquals("feryel", results.get(0).getUsername());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoPosts() {
        when(postRepository.findByUserId(1L)).thenReturn(List.of());

        List<PostDto> results = service.getPostsByUserId(1L);

        assertTrue(results.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO mapping (toDto) — verified through public methods
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldMapAllFieldsCorrectlyInDto() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 31, 12, 30);
        post.setCreationDate(now);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        PostDto dto = service.getPostById(10L).orElseThrow();

        assertEquals(10L,       dto.getId());
        assertEquals("Hello community!", dto.getContent());
        assertEquals(now,        dto.getCreationDate());
        assertEquals(1L,         dto.getUserId());
        assertEquals("feryel",   dto.getUsername());
    }
}
