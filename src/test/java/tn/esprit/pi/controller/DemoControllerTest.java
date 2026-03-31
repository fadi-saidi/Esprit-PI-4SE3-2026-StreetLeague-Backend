package tn.esprit.pi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - DemoController")
class DemoControllerTest {

    @InjectMocks
    private DemoController demoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(demoController).build();
    }

    @Test
    @DisplayName("GET /student/hello - Devrait retourner Hello STUDENT")
    void etu_Success() throws Exception {
        mockMvc.perform(get("/student/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello STUDENT"));
    }

    @Test
    @DisplayName("GET /teacher/hello - Devrait retourner Hello TEACHER")
    void ens_Success() throws Exception {
        mockMvc.perform(get("/teacher/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello TEACHER"));
    }
}