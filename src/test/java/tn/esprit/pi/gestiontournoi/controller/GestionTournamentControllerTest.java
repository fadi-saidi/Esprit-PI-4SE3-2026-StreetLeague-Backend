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
import tn.esprit.pi.gestiontournoi.entity.GestionTournament;
import tn.esprit.pi.gestiontournoi.repository.GestionTournamentRepository;
import tn.esprit.pi.security.GlobalExceptionHandler;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionTournamentController")
class GestionTournamentControllerTest {

    @Mock
    private GestionTournamentRepository repository;

    @InjectMocks
    private GestionTournamentController controller;

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
    void getAll_returnsTournaments() throws Exception {
        GestionTournament tournament = new GestionTournament();
        tournament.setId(1L);
        tournament.setName("Summer Cup");
        tournament.setStartDate("2026-06-01");
        tournament.setEndDate("2026-06-10");
        tournament.setLocation("Arena");
        tournament.setMaxTeams(8);

        when(repository.findAll(any(Sort.class))).thenReturn(List.of(tournament));

        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Summer Cup"))
                .andExpect(jsonPath("$[0].maxTeams").value(8));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "startDate": "",
                                  "endDate": "",
                                  "location": "",
                                  "maxTeams": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.startDate").exists())
                .andExpect(jsonPath("$.errors.endDate").exists())
                .andExpect(jsonPath("$.errors.location").exists())
                .andExpect(jsonPath("$.errors.maxTeams").exists());
    }

    @Test
    void update_existingTournament_returnsUpdatedTournament() throws Exception {
        GestionTournament existing = new GestionTournament();
        existing.setId(6L);
        existing.setName("Old Cup");
        existing.setStartDate("2026-05-01");
        existing.setEndDate("2026-05-03");
        existing.setLocation("Old Arena");
        existing.setMaxTeams(8);

        when(repository.findById(6L)).thenReturn(Optional.of(existing));
        when(repository.save(any(GestionTournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/tournaments/6")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Updated Cup",
                                  "startDate": "2026-05-10",
                                  "endDate": "2026-05-12",
                                  "location": "New Arena",
                                  "maxTeams": 16
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Cup"))
                .andExpect(jsonPath("$.location").value("New Arena"))
                .andExpect(jsonPath("$.maxTeams").value(16));

        verify(repository).save(existing);
    }

    @Test
    void delete_existingTournament_returnsNoContent() throws Exception {
        when(repository.existsById(8L)).thenReturn(true);

        mockMvc.perform(delete("/api/tournaments/8"))
                .andExpect(status().isNoContent());

        verify(repository).deleteById(8L);
    }
}
