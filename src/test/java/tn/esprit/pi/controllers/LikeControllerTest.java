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
import tn.esprit.pi.controller.LikeController;
import tn.esprit.pi.domain.Like;
import tn.esprit.pi.domain.LikeType;
import tn.esprit.pi.dto.LikeDto;
import tn.esprit.pi.service.ILikeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    @Mock
    private ILikeService likeService;

    @InjectMocks
    private LikeController likeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(likeController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private Like buildLike(Long id, Long postId, Long userId, LikeType likeType) {
        Like like = new Like();
        like.setId(id);
        // assuming Post and User have getId()
        tn.esprit.pi.domain.Post post = new tn.esprit.pi.domain.Post();
        post.setId(postId);
        tn.esprit.pi.domain.User user = new tn.esprit.pi.domain.User();
        user.setId(userId);
        like.setPost(post);
        like.setUser(user);
        like.setLikeType(likeType);
        like.setCreationDate(LocalDateTime.now());
        return like;
    }

    @Test
    void shouldCreateLike() throws Exception {
        Like like = buildLike(1L, 1L, 2L, LikeType.LOVE);
        when(likeService.createLike(1L, 2L, LikeType.LOVE)).thenReturn(like);

        mockMvc.perform(post("/likes")
                        .param("postId", "1")
                        .param("userId", "2")
                        .param("likeType", "LOVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.postId").value(1L))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.likeType").value("LOVE"));

        verify(likeService, times(1)).createLike(1L, 2L, LikeType.LOVE);
    }

    @Test
    void shouldGetAllLikes() throws Exception {
        List<Like> likes = List.of(
                buildLike(1L, 1L, 2L, LikeType.LOVE),
                buildLike(2L, 1L, 3L, LikeType.LIKE)
        );
        when(likeService.getAllLikes()).thenReturn(likes);

        mockMvc.perform(get("/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(likeService, times(1)).getAllLikes();
    }

    @Test
    void shouldGetLikeById() throws Exception {
        Like like = buildLike(1L, 1L, 2L, LikeType.LOVE);
        when(likeService.getLikeById(1L)).thenReturn(Optional.of(like));

        mockMvc.perform(get("/likes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.likeType").value("LOVE"));

        verify(likeService, times(1)).getLikeById(1L);
    }

    @Test
    void shouldReturn404WhenLikeNotFound() throws Exception {
        when(likeService.getLikeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/likes/99"))
                .andExpect(status().isNotFound());

        verify(likeService, times(1)).getLikeById(99L);
    }

    @Test
    void shouldDeleteLike() throws Exception {
        doNothing().when(likeService).deleteLike(1L);

        mockMvc.perform(delete("/likes/1"))
                .andExpect(status().isNoContent());

        verify(likeService, times(1)).deleteLike(1L);
    }

    @Test
    void shouldGetLikesByPost() throws Exception {
        List<Like> likes = List.of(buildLike(1L, 1L, 2L, LikeType.LOVE));
        when(likeService.getLikesByPost(1L)).thenReturn(likes);

        mockMvc.perform(get("/likes/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].postId").value(1L));

        verify(likeService, times(1)).getLikesByPost(1L);
    }

    @Test
    void shouldGetLikesByUser() throws Exception {
        List<Like> likes = List.of(buildLike(1L, 1L, 2L, LikeType.LOVE));
        when(likeService.getLikesByUser(2L)).thenReturn(likes);

        mockMvc.perform(get("/likes/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(2L));

        verify(likeService, times(1)).getLikesByUser(2L);
    }
}