package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.Comment;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.CommentRepository;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.CommentServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl service;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Post post;
    private Comment comment;

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

        comment = new Comment();
        comment.setId(100L);
        comment.setContent("Nice post!");
        comment.setCreationDate(LocalDateTime.of(2026, 3, 31, 11, 0));
        comment.setPost(post);
        comment.setUser(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createComment
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateComment() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = service.createComment(10L, 1L, "Nice post!");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Nice post!", result.getContent());
        assertEquals(post, result.getPost());
        assertEquals(user, result.getUser());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnCreate() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createComment(99L, 1L, "content"));

        assertTrue(ex.getMessage().contains("99"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundOnCreate() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createComment(10L, 99L, "content"));

        assertTrue(ex.getMessage().contains("99"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldLinkPostAndUserViaHelperOnCreate() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = service.createComment(10L, 1L, "Nice!");

        // post.addComment(comment) sets comment.post
        assertEquals(post, result.getPost());
        // user.addComment(comment) sets comment.user
        assertEquals(user, result.getUser());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // replyToComment
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateReply() {
        Comment reply = new Comment();
        reply.setId(200L);
        reply.setContent("Great point!");
        reply.setParentComment(comment);
        reply.setPost(post);
        reply.setUser(user);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(reply);

        Comment result = service.replyToComment(100L, 1L, "Great point!");

        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals("Great point!", result.getContent());
        assertEquals(comment, result.getParentComment());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowWhenParentCommentNotFoundOnReply() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.replyToComment(99L, 1L, "content"));

        assertTrue(ex.getMessage().contains("99"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundOnReply() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.replyToComment(100L, 99L, "content"));

        assertTrue(ex.getMessage().contains("99"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldInheritParentPostWhenReplying() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = service.replyToComment(100L, 1L, "Reply!");

        // addReply() sets reply.post = parentComment.post
        assertEquals(post, result.getPost());
        // addReply() sets reply.parentComment = comment
        assertEquals(comment, result.getParentComment());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCommentsByPost
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnCommentsForGivenPost() {
        when(commentRepository.findByPostId(10L)).thenReturn(List.of(comment));

        List<Comment> results = service.getCommentsByPost(10L);

        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).getId());
    }

    @Test
    void shouldReturnEmptyListWhenPostHasNoComments() {
        when(commentRepository.findByPostId(10L)).thenReturn(List.of());

        List<Comment> results = service.getCommentsByPost(10L);

        assertTrue(results.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCommentById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnCommentWhenExists() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        Optional<Comment> result = service.getCommentById(100L);

        assertTrue(result.isPresent());
        assertEquals("Nice post!", result.get().getContent());
    }

    @Test
    void shouldReturnEmptyOptionalWhenCommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Comment> result = service.getCommentById(99L);

        assertFalse(result.isPresent());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateComment
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateCommentContent() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = service.updateComment(100L, "Updated content");

        assertEquals("Updated content", result.getContent());
        verify(commentRepository).save(comment);
    }

    @Test
    void shouldNotTouchRelationsOnUpdate() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = service.updateComment(100L, "Changed");

        // post and user links must stay intact
        assertEquals(post, result.getPost());
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldThrowWhenCommentNotFoundOnUpdate() {
        when(commentRepository.findById(55L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateComment(55L, "anything"));

        assertTrue(ex.getMessage().contains("55"));
        verify(commentRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteComment
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldDeleteCommentAndUnlinkFromPost() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        service.deleteComment(100L);

        // post.removeComment() must have been triggered → comment.post is now null
        assertNull(comment.getPost());
        verify(commentRepository).delete(comment);
    }

    @Test
    void shouldThrowWhenCommentNotFoundOnDelete() {
        when(commentRepository.findById(55L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteComment(55L));

        assertTrue(ex.getMessage().contains("55"));
        verify(commentRepository, never()).delete(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getRepliesByCommentId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnRepliesForGivenComment() {
        Comment reply = new Comment();
        reply.setId(200L);
        reply.setContent("Great point!");
        reply.setParentComment(comment);
        reply.setPost(post);
        reply.setUser(user);

        when(commentRepository.findByParentCommentId(100L)).thenReturn(List.of(reply));

        List<Comment> results = service.getRepliesByCommentId(100L);

        assertEquals(1, results.size());
        assertEquals(200L, results.get(0).getId());
        assertEquals(comment, results.get(0).getParentComment());
    }

    @Test
    void shouldReturnEmptyListWhenNoReplies() {
        when(commentRepository.findByParentCommentId(100L)).thenReturn(List.of());

        List<Comment> results = service.getRepliesByCommentId(100L);

        assertTrue(results.isEmpty());
    }
}
