package tn.esprit.pi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.controller.CommentController;
import tn.esprit.pi.domain.Comment;
import tn.esprit.pi.dto.CommentDto;
import tn.esprit.pi.service.ICommentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private ICommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    private Comment buildComment(Long id, Long postId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setCreationDate(LocalDateTime.now());
        tn.esprit.pi.domain.Post post = new tn.esprit.pi.domain.Post();
        post.setId(postId);
        comment.setPost(post);

        tn.esprit.pi.domain.User user = new tn.esprit.pi.domain.User();
        user.setId(userId);
        user.setUsername("user" + userId);
        comment.setUser(user);

        if (parentId != null) {
            Comment parent = new Comment();
            parent.setId(parentId);
            comment.setParentComment(parent);
        }

        return comment;
    }

    @Test
    void shouldCreateComment() throws Exception {
        Comment comment = buildComment(1L, 1L, 2L, "Nice post!", null);
        when(commentService.createComment(1L, 2L, "Nice post!")).thenReturn(comment);

        mockMvc.perform(post("/comments")
                        .param("postId", "1")
                        .param("userId", "2")
                        .param("content", "Nice post!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Nice post!"))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.postId").value(1L));

        verify(commentService, times(1)).createComment(1L, 2L, "Nice post!");
    }

    @Test
    void shouldReplyToComment() throws Exception {
        Comment reply = buildComment(2L, 1L, 3L, "I agree!", 1L);
        when(commentService.replyToComment(1L, 3L, "I agree!")).thenReturn(reply);

        mockMvc.perform(post("/comments/reply")
                        .param("parentCommentId", "1")
                        .param("userId", "3")
                        .param("content", "I agree!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.content").value("I agree!"))
                .andExpect(jsonPath("$.parentCommentId").value(1L));

        verify(commentService, times(1)).replyToComment(1L, 3L, "I agree!");
    }

    @Test
    void shouldGetCommentsByPost() throws Exception {
        List<Comment> comments = List.of(
                buildComment(1L, 1L, 2L, "Hello", null),
                buildComment(2L, 1L, 3L, "World", null)
        );
        when(commentService.getCommentsByPost(1L)).thenReturn(comments);

        mockMvc.perform(get("/comments/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(commentService, times(1)).getCommentsByPost(1L);
    }

    @Test
    void shouldGetCommentById() throws Exception {
        Comment comment = buildComment(1L, 1L, 2L, "Hello", null);
        when(commentService.getCommentById(1L)).thenReturn(Optional.of(comment));

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Hello"));

        verify(commentService, times(1)).getCommentById(1L);
    }

    @Test
    void shouldReturn404WhenCommentNotFound() throws Exception {
        when(commentService.getCommentById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/comments/99"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getCommentById(99L);
    }

    @Test
    void shouldUpdateComment() throws Exception {
        Comment updated = buildComment(1L, 1L, 2L, "Updated content", null);
        when(commentService.updateComment(1L, "Updated content")).thenReturn(updated);

        mockMvc.perform(put("/comments/1")
                        .param("content", "Updated content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Updated content"));

        verify(commentService, times(1)).updateComment(1L, "Updated content");
    }

    @Test
    void shouldDeleteComment() throws Exception {
        doNothing().when(commentService).deleteComment(1L);

        mockMvc.perform(delete("/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(1L);
    }

    @Test
    void shouldGetRepliesByCommentId() throws Exception {
        List<Comment> replies = List.of(buildComment(2L, 1L, 3L, "Reply!", 1L));
        when(commentService.getRepliesByCommentId(1L)).thenReturn(replies);

        mockMvc.perform(get("/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].parentCommentId").value(1L));

        verify(commentService, times(1)).getRepliesByCommentId(1L);
    }
}