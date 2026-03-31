package tn.esprit.pi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.Shop;
import tn.esprit.pi.repository.ShopRepository;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - PublicShopController")
class PublicShopControllerTest {

    @Mock private ShopRepository shopRepository;
    @InjectMocks private PublicShopController publicShopController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicShopController).build();
    }

    @Test
    @DisplayName("GET /shops - Succès")
    void getAllShops_Success() throws Exception {
        Shop shop = new Shop();
        shop.setName("Boutique Sport");
        when(shopRepository.findAll()).thenReturn(Collections.singletonList(shop));

        mockMvc.perform(get("/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Boutique Sport"));
    }

    @Test
    @DisplayName("GET /shops/{id} - Non trouvé")
    void getShopById_NotFound() throws Exception {
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/shops/1"))
                .andExpect(status().isNotFound());
    }
}