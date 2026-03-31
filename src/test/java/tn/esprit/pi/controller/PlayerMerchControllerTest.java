package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.service.WalletService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PlayerMerchControllerTest {

    @Mock private PlayerMerchRepository playerMerchRepository;
    @Mock private PlayerProfileRepository playerProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private WalletService walletService;
    @Mock private UserDetails userDetails;

    @InjectMocks private PlayerMerchController playerMerchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(playerMerchController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(UserDetails.class);
                    }
                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return userDetails;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("POST /submit - Succès pour un Player")
    void submitMerch_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.PLAYER);
        user.setUsername("player1");

        PlayerProfile profile = new PlayerProfile();
        profile.setId(1L);

        ShopDTOs.PlayerMerchRequest request = new ShopDTOs.PlayerMerchRequest(
                "Maillot", "Desc", 50.0, 10, "Sport", "url", SportType.FOOTBALL
        );

        when(userDetails.getUsername()).thenReturn("player@test.tn");
        when(userRepository.findByEmail("player@test.tn")).thenReturn(Optional.of(user));
        when(playerProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        // CORRECTION ICI : Ajout du cast (PlayerMerch)
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenAnswer(i -> {
            PlayerMerch m = (PlayerMerch) i.getArguments()[0];
            m.setId(1L);
            return m;
        });

        mockMvc.perform(post("/player-merch/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maillot"));
    }

    @Test
    @DisplayName("PUT /admin/{id}/reject - Succès")
    void rejectMerch_Success() throws Exception {
        PlayerMerch merch = new PlayerMerch();
        merch.setId(1L);
        merch.setStatus(MerchStatus.PENDING);

        User admin = new User();
        admin.setRole(Role.ADMIN);

        ShopDTOs.MerchApprovalRequest rejectReq = new ShopDTOs.MerchApprovalRequest("Mauvaise qualité");

        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(merch));
        when(userDetails.getUsername()).thenReturn("admin@test.tn");
        when(userRepository.findByEmail("admin@test.tn")).thenReturn(Optional.of(admin));
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenReturn(merch);

        mockMvc.perform(put("/player-merch/admin/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /approved - Succès (Pagination)")
    void getApprovedMerch_Success() throws Exception {
        // Mock de la page retournée par le repository
        when(playerMerchRepository.findByStatusOrderBySubmittedAtDesc(any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/player-merch/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("PUT /admin/{id}/approve - Forbidden si non Admin")
    void approveMerch_Forbidden() throws Exception {
        PlayerMerch merch = new PlayerMerch();
        merch.setId(1L);
        User nonAdmin = new User();
        nonAdmin.setRole(Role.PLAYER);

        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(merch));
        when(userDetails.getUsername()).thenReturn("user@test.tn");
        when(userRepository.findByEmail("user@test.tn")).thenReturn(Optional.of(nonAdmin));

        mockMvc.perform(put("/player-merch/admin/1/approve"))
                .andExpect(status().isForbidden());
    }
}