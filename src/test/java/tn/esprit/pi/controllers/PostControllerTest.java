package tn.esprit.pi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.controller.PostController;
import tn.esprit.pi.dto.PostDto;
import tn.esprit.pi.service.IPostService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private IPostService postService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    private PostDto buildPostDto(Long id, Long userId, String content) {
        PostDto dto = new PostDto();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setContent(content);
        return dto;
    }

    @Test
    void shouldCreatePost() throws Exception {
        PostDto dto = buildPostDto(1L, 1L, "Hello World");
        when(postService.createPost(1L, "Hello World")).thenReturn(dto);

        mockMvc.perform(post("/posts")
                        .param("userId", "1")
                        .param("content", "Hello World"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.content").value("Hello World"));

        verify(postService, times(1)).createPost(1L, "Hello World");
    }

    @Test
    void shouldGetAllPosts() throws Exception {
        List<PostDto> posts = List.of(
                buildPostDto(1L, 1L, "Post 1"),
                buildPostDto(2L, 2L, "Post 2")
        );
        when(postService.getAllPosts()).thenReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(postService, times(1)).getAllPosts();
    }

    @Test
    void shouldGetPostById() throws Exception {
        PostDto dto = buildPostDto(1L, 1L, "Hello World");
        when(postService.getPostById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Hello World"));

        verify(postService, times(1)).getPostById(1L);
    }

    @Test
    void shouldReturn404WhenPostNotFound() throws Exception {
        when(postService.getPostById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/99"))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getPostById(99L);
    }

    @Test
    void shouldUpdatePost() throws Exception {
        PostDto updated = buildPostDto(1L, 1L, "Updated content");
        when(postService.updatePost(1L, "Updated content")).thenReturn(updated);

        mockMvc.perform(put("/posts/1")
                        .param("content", "Updated content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Updated content"));

        verify(postService, times(1)).updatePost(1L, "Updated content");
    }

    @Test
    void shouldDeletePost() throws Exception {
        doNothing().when(postService).deletePost(1L);

        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(1L);
    }

    @Test
    void shouldGetPostsByUser() throws Exception {
        List<PostDto> posts = List.of(buildPostDto(1L, 1L, "My post"));
        when(postService.getPostsByUserId(1L)).thenReturn(posts);

        mockMvc.perform(get("/posts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1L));

        verify(postService, times(1)).getPostsByUserId(1L);
    }
}