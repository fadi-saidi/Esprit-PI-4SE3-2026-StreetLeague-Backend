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
import tn.esprit.pi.gestiontournoi.entity.GestionTraining;
import tn.esprit.pi.gestiontournoi.repository.GestionTrainingRepository;
import tn.esprit.pi.security.GlobalExceptionHandler;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionTrainingController")
class GestionTrainingControllerTest {

    @Mock
    private GestionTrainingRepository repository;

    @InjectMocks
    private GestionTrainingController controller;

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
    void getAll_returnsTrainings() throws Exception {
        GestionTraining training = new GestionTraining();
        training.setId(1L);
        training.setTitle("Morning Session");
        training.setCoach("Coach Karim");
        training.setDate("2026-04-22");
        training.setDuration(90);
        training.setLocation("Gym Hall");

        when(repository.findAll(any(Sort.class))).thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Morning Session"))
                .andExpect(jsonPath("$[0].duration").value(90));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "",
                                  "coach": "",
                                  "date": "",
                                  "duration": 0,
                                  "location": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.coach").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.duration").exists())
                .andExpect(jsonPath("$.errors.location").exists());
    }

    @Test
    void update_existingTraining_returnsUpdatedTraining() throws Exception {
        GestionTraining existing = new GestionTraining();
        existing.setId(5L);
        existing.setTitle("Old Session");
        existing.setCoach("Coach A");
        existing.setDate("2026-04-01");
        existing.setDuration(60);
        existing.setLocation("Old Hall");

        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(any(GestionTraining.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/trainings/5")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Updated Session",
                                  "coach": "Coach B",
                                  "date": "2026-04-30",
                                  "duration": 120,
                                  "location": "New Hall"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Session"))
                .andExpect(jsonPath("$.coach").value("Coach B"))
                .andExpect(jsonPath("$.duration").value(120));

        verify(repository).save(existing);
    }

    @Test
    void delete_existingTraining_returnsNoContent() throws Exception {
        when(repository.existsById(11L)).thenReturn(true);

        mockMvc.perform(delete("/api/trainings/11"))
                .andExpect(status().isNoContent());

        verify(repository).deleteById(11L);
    }
}
