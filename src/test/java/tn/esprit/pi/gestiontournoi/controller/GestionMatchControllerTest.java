package tn.esprit.pi.gestiontournoi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tn.esprit.pi.gestiontournoi.entity.GestionMatch;
import tn.esprit.pi.gestiontournoi.repository.GestionMatchRepository;
import tn.esprit.pi.security.GlobalExceptionHandler;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionMatchController")
class GestionMatchControllerTest {

    @Mock
    private GestionMatchRepository repository;

    @InjectMocks
    private GestionMatchController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getAll_returnsMatches() throws Exception {
        GestionMatch match = new GestionMatch();
        match.setId(1L);
        match.setHomeTeam("Team A");
        match.setAwayTeam("Team B");
        match.setDate("2026-04-15");
        match.setScore("1-0");
        match.setLocation("Pitch 1");

        when(repository.findAll(any(Sort.class))).thenReturn(List.of(match));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].homeTeam").value("Team A"))
                .andExpect(jsonPath("$[0].score").value("1-0"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType("application/json")
                        .content("""
                                {
                                  "homeTeam": "",
                                  "awayTeam": "",
                                  "date": "",
                                  "score": "0-0",
                                  "location": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.homeTeam").exists())
                .andExpect(jsonPath("$.errors.awayTeam").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.location").exists());
    }

    @Test
    void update_existingMatch_returnsUpdatedMatch() throws Exception {
        GestionMatch existing = new GestionMatch();
        existing.setId(3L);
        existing.setHomeTeam("Old Home");
        existing.setAwayTeam("Old Away");
        existing.setDate("2026-04-01");
        existing.setScore("0-0");
        existing.setLocation("Old Field");

        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.save(any(GestionMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/matches/3")
                        .contentType("application/json")
                        .content("""
                                {
                                  "homeTeam": "New Home",
                                  "awayTeam": "New Away",
                                  "date": "2026-04-20",
                                  "score": "3-2",
                                  "location": "Field 2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeTeam").value("New Home"))
                .andExpect(jsonPath("$.awayTeam").value("New Away"))
                .andExpect(jsonPath("$.score").value("3-2"));

        verify(repository).save(existing);
    }

    @Test
    void delete_existingMatch_returnsNoContent() throws Exception {
        when(repository.existsById(4L)).thenReturn(true);

        mockMvc.perform(delete("/api/matches/4"))
                .andExpect(status().isNoContent());

        verify(repository).deleteById(4L);
    }
}
