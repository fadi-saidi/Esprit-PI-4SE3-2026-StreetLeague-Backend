package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.security.jwt.JwtService;
import tn.esprit.pi.service.WalletService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerMerchController.class)
class PlayerMerchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerMerchRepository playerMerchRepository;
    @MockitoBean
    private PlayerProfileRepository playerProfileRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private ShopRepository shopRepository;
    @MockitoBean
    private WalletService walletService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private tn.esprit.pi.security.CustomUserDetailsService customUserDetailsService;

    private ObjectMapper objectMapper;
    private User playerUser;
    private User adminUser;
    private PlayerProfile playerProfile;
    private PlayerMerch playerMerch;
    private Shop shop;
    private Product product;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        playerUser = new User();
        playerUser.setId(1L);
        playerUser.setEmail("player@test.com");
        playerUser.setUsername("testplayer");
        playerUser.setRole(Role.PLAYER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setUsername("testadmin");
        adminUser.setRole(Role.ADMIN);

        playerProfile = new PlayerProfile();
        playerProfile.setId(1L);
        playerProfile.setUser(playerUser);

        playerMerch = new PlayerMerch();
        playerMerch.setId(1L);
        playerMerch.setName("Test Jersey");
        playerMerch.setDescription("Player custom jersey");
        playerMerch.setPrice(50.0);
        playerMerch.setStock(10);
        playerMerch.setCategory("Apparel");
        playerMerch.setImage("jersey.jpg");
        playerMerch.setSportType(SportType.FOOTBALL);
        playerMerch.setStatus(MerchStatus.PENDING);
        playerMerch.setSeller(playerProfile);
        playerMerch.setSubmittedAt(LocalDateTime.now());

        shop = new Shop();
        shop.setId(1L);
        shop.setName("Player Marketplace");
        shop.setDescription("Player-submitted merchandise");

        product = new Product();
        product.setId(1L);
        product.setName("Test Jersey");
        product.setPrice(50.0);
        product.setStock(10);
        product.setShop(shop);
    }

    @Test
    void getApprovedMerch_ShouldReturnPagedResults() throws Exception {
        PlayerMerch approvedMerch = new PlayerMerch();
        approvedMerch.setId(2L);
        approvedMerch.setName("Approved Jersey");
        approvedMerch.setStatus(MerchStatus.APPROVED);
        approvedMerch.setSeller(playerProfile);

        Page<PlayerMerch> page = new PageImpl<>(List.of(approvedMerch), PageRequest.of(0, 20), 1);
        when(playerMerchRepository.findByStatusOrderBySubmittedAtDesc(eq(MerchStatus.APPROVED), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/player-merch/approved")
                .param("page", "0")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Approved Jersey"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getMerchById_ShouldReturnMerch() throws Exception {
        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));

        mockMvc.perform(get("/player-merch/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Jersey"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getMerchById_ShouldReturnNotFound() throws Exception {
        when(playerMerchRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/player-merch/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void submitMerch_ShouldCreateMerch() throws Exception {
        PlayerMerchRequest request = new PlayerMerchRequest(
                "New Jersey",
                "Custom player jersey",
                75.0,
                5,
                "Apparel",
                "new-jersey.jpg",
                SportType.FOOTBALL
        );

        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));
        when(playerProfileRepository.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenReturn(playerMerch);

        mockMvc.perform(post("/player-merch/submit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Jersey"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void submitMerch_ShouldReturnUnauthorizedWithoutAuth() throws Exception {
        PlayerMerchRequest request = new PlayerMerchRequest(
                "New Jersey", "Custom jersey", 75.0, 5, "Apparel", "jersey.jpg", SportType.FOOTBALL
        );

        mockMvc.perform(post("/player-merch/submit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void submitMerch_ShouldReturnForbiddenForNonPlayer() throws Exception {
        PlayerMerchRequest request = new PlayerMerchRequest(
                "New Jersey", "Custom jersey", 75.0, 5, "Apparel", "jersey.jpg", SportType.FOOTBALL
        );

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/player-merch/submit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only players can submit merchandise"));
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void getMySubmissions_ShouldReturnPlayerMerch() throws Exception {
        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));
        when(playerProfileRepository.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(playerMerchRepository.findBySellerOrderBySubmittedAtDesc(playerProfile))
                .thenReturn(List.of(playerMerch));

        mockMvc.perform(get("/player-merch/my-submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Jersey"));
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void updateMerch_ShouldUpdatePendingMerch() throws Exception {
        PlayerMerchRequest request = new PlayerMerchRequest(
                "Updated Jersey", "Updated description", 60.0, 8, "Apparel", "updated.jpg", SportType.FOOTBALL
        );

        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));
        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenReturn(playerMerch);

        mockMvc.perform(put("/player-merch/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Jersey"));
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void updateMerch_ShouldReturnBadRequestForApprovedMerch() throws Exception {
        playerMerch.setStatus(MerchStatus.APPROVED);
        PlayerMerchRequest request = new PlayerMerchRequest(
                "Updated Jersey", "Updated description", 60.0, 8, "Apparel", "updated.jpg", SportType.FOOTBALL
        );

        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));
        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));

        mockMvc.perform(put("/player-merch/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Can only update pending merchandise"));
    }

    @Test
    void getPendingMerch_ShouldReturnPagedResults() throws Exception {
        Page<PlayerMerch> page = new PageImpl<>(List.of(playerMerch), PageRequest.of(0, 20), 1);
        when(playerMerchRepository.findByStatusOrderBySubmittedAtDesc(eq(MerchStatus.PENDING), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/player-merch/admin/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getAdminStats_ShouldReturnStats() throws Exception {
        when(playerMerchRepository.countByStatus(MerchStatus.PENDING)).thenReturn(5L);
        when(playerMerchRepository.countByStatus(MerchStatus.APPROVED)).thenReturn(10L);
        when(playerMerchRepository.countByStatus(MerchStatus.REJECTED)).thenReturn(2L);

        mockMvc.perform(get("/player-merch/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(5))
                .andExpect(jsonPath("$.approved").value(10))
                .andExpect(jsonPath("$.rejected").value(2));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void approveMerch_ShouldApproveAndCreateProduct() throws Exception {
        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(shopRepository.findAll()).thenReturn(List.of(shop));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenReturn(playerMerch);

        mockMvc.perform(put("/player-merch/admin/1/approve")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Jersey"));
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void approveMerch_ShouldReturnForbiddenForNonAdmin() throws Exception {
        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));
        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));

        mockMvc.perform(put("/player-merch/admin/1/approve")
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Admin access required"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void rejectMerch_ShouldRejectWithReason() throws Exception {
        MerchApprovalRequest request = new MerchApprovalRequest("Quality issues");

        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenReturn(playerMerch);

        mockMvc.perform(put("/player-merch/admin/1/reject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Jersey"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void approveMerch_ShouldReturnBadRequestForAlreadyApproved() throws Exception {
        playerMerch.setStatus(MerchStatus.APPROVED);

        when(playerMerchRepository.findById(1L)).thenReturn(Optional.of(playerMerch));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(put("/player-merch/admin/1/approve")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Can only approve pending merchandise"));
    }

    @Test
    void approveMerch_ShouldReturnNotFoundForInvalidId() throws Exception {
        when(playerMerchRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/player-merch/admin/999/approve")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}