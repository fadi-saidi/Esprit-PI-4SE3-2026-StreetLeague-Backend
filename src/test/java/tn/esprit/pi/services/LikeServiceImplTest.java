package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.Like;
import tn.esprit.pi.domain.LikeType;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.LikeRepository;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.LikeServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @InjectMocks
    private LikeServiceImpl service;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Post post;
    private Like like;

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

        like = new Like();
        like.setId(50L);
        like.setLikeType(LikeType.LIKE);
        like.setPost(post);
        like.setUser(user);
        like.setCreationDate(LocalDateTime.of(2026, 3, 31, 11, 0));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createLike — new like (no existing)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateNewLikeWhenNoneExists() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(like);

        Like result = service.createLike(10L, 1L, LikeType.LIKE);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals(LikeType.LIKE, result.getLikeType());
        assertEquals(post, result.getPost());
        assertEquals(user, result.getUser());
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void shouldSetPostAndUserOnNewLike() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenAnswer(inv -> inv.getArgument(0));

        Like result = service.createLike(10L, 1L, LikeType.LOVE);

        assertEquals(post, result.getPost());
        assertEquals(user, result.getUser());
        assertEquals(LikeType.LOVE, result.getLikeType());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createLike — update existing like (upsert behaviour)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateLikeTypeWhenLikeAlreadyExists() {
        Like existing = new Like();
        existing.setId(50L);
        existing.setLikeType(LikeType.LIKE);
        existing.setPost(post);
        existing.setUser(user);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(likeRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        Like result = service.createLike(10L, 1L, LikeType.LOVE);

        assertEquals(LikeType.LOVE, result.getLikeType());
        assertEquals(50L, result.getId()); // same record, not a new one
        verify(likeRepository).save(existing);
    }

    @Test
    void shouldNotCreateNewRecordOnUpsert() {
        Like existing = new Like();
        existing.setId(50L);
        existing.setLikeType(LikeType.LIKE);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));
        when(likeRepository.save(any(Like.class))).thenReturn(existing);

        service.createLike(10L, 1L, LikeType.HAHA);

        // save must be called exactly once — no extra insert
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createLike — not-found guards
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenPostNotFoundOnCreate() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createLike(99L, 1L, LikeType.LIKE));

        assertTrue(ex.getMessage().contains("99"));
        verify(likeRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundOnCreate() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createLike(10L, 99L, LikeType.LIKE));

        assertTrue(ex.getMessage().contains("99"));
        verify(likeRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllLikes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnAllLikes() {
        Like second = new Like();
        second.setId(51L);
        second.setLikeType(LikeType.LOVE);
        second.setPost(post);
        second.setUser(user);

        when(likeRepository.findAll()).thenReturn(List.of(like, second));

        List<Like> results = service.getAllLikes();

        assertEquals(2, results.size());
        assertEquals(50L, results.get(0).getId());
        assertEquals(51L, results.get(1).getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoLikesExist() {
        when(likeRepository.findAll()).thenReturn(List.of());

        List<Like> results = service.getAllLikes();

        assertTrue(results.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getLikeById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnLikeWhenExists() {
        when(likeRepository.findById(50L)).thenReturn(Optional.of(like));

        Optional<Like> result = service.getLikeById(50L);

        assertTrue(result.isPresent());
        assertEquals(LikeType.LIKE, result.get().getLikeType());
    }

    @Test
    void shouldReturnEmptyOptionalWhenLikeNotFound() {
        when(likeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Like> result = service.getLikeById(99L);

        assertFalse(result.isPresent());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteLike
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldDeleteLikeById() {
        doNothing().when(likeRepository).deleteById(50L);

        service.deleteLike(50L);

        verify(likeRepository).deleteById(50L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getLikesByPost
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnLikesForGivenPost() {
        when(likeRepository.findByPostId(10L)).thenReturn(List.of(like));

        List<Like> results = service.getLikesByPost(10L);

        assertEquals(1, results.size());
        assertEquals(post, results.get(0).getPost());
    }

    @Test
    void shouldReturnEmptyListWhenPostHasNoLikes() {
        when(likeRepository.findByPostId(10L)).thenReturn(List.of());

        List<Like> results = service.getLikesByPost(10L);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnAllLikeTypesForPost() {
        Like loveLike = new Like();
        loveLike.setId(51L);
        loveLike.setLikeType(LikeType.LOVE);
        loveLike.setPost(post);
        loveLike.setUser(user);

        when(likeRepository.findByPostId(10L)).thenReturn(List.of(like, loveLike));

        List<Like> results = service.getLikesByPost(10L);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(l -> l.getLikeType() == LikeType.LIKE));
        assertTrue(results.stream().anyMatch(l -> l.getLikeType() == LikeType.LOVE));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getLikesByUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnLikesForGivenUser() {
        when(likeRepository.findByUserId(1L)).thenReturn(List.of(like));

        List<Like> results = service.getLikesByUser(1L);

        assertEquals(1, results.size());
        assertEquals(user, results.get(0).getUser());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoLikes() {
        when(likeRepository.findByUserId(1L)).thenReturn(List.of());

        List<Like> results = service.getLikesByUser(1L);

        assertTrue(results.isEmpty());
    }
}
