package tn.esprit.pi.gestiontournoi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;
import tn.esprit.pi.gestiontournoi.repository.GestionEventRepository;
import tn.esprit.pi.security.GlobalExceptionHandler;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionEventController")
class GestionEventControllerTest {

    @Mock
    private GestionEventRepository repository;

    @InjectMocks
    private GestionEventController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void getAll_returnsEvents() throws Exception {
        GestionEvent event = new GestionEvent();
        event.setId(1L);
        event.setName("Quarter Finals");
        event.setDescription("Knockout round");
        event.setDate("2026-04-10");
        event.setLocation("Main Arena");

        when(repository.findAll(any(Sort.class))).thenReturn(List.of(event));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Quarter Finals"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "description": "desc",
                                  "date": "",
                                  "location": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.location").exists());
    }

    @Test
    void update_existingEvent_returnsUpdatedEvent() throws Exception {
        GestionEvent existing = new GestionEvent();
        existing.setId(7L);
        existing.setName("Old Name");
        existing.setDescription("Old");
        existing.setDate("2026-04-01");
        existing.setLocation("Old Place");

        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any(GestionEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/events/7")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "New Name",
                                  "description": "Updated",
                                  "date": "2026-04-12",
                                  "location": "New Place"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.location").value("New Place"));

        verify(repository).save(existing);
    }

    @Test
    void delete_existingEvent_returnsNoContent() throws Exception {
        when(repository.existsById(9L)).thenReturn(true);

        mockMvc.perform(delete("/api/events/9"))
                .andExpect(status().isNoContent());

        verify(repository).deleteById(9L);
    }
}
