package tn.esprit.pi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.Shop;
import tn.esprit.pi.repository.ShopRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - ShopController (Admin)")
class ShopControllerTest {

    @Mock private ShopRepository shopRepository;
    @InjectMocks private ShopController shopController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(shopController).build();
    }

    @Test
    @DisplayName("POST /admin/shops - Création")
    void createShop_Success() throws Exception {
        Shop shop = new Shop();
        shop.setName("Admin Shop");
        when(shopRepository.save(any(Shop.class))).thenReturn(shop);

        mockMvc.perform(post("/admin/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin Shop\", \"description\":\"Test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Shop"));
    }

    @Test
    @DisplayName("PUT /admin/shops/{id} - Mise à jour")
    void updateShop_Success() throws Exception {
        Shop existingShop = new Shop();
        existingShop.setId(1L);
        existingShop.setName("Old Name");

        when(shopRepository.findById(1L)).thenReturn(Optional.of(existingShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(existingShop);

        mockMvc.perform(put("/admin/shops/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\", \"description\":\"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @DisplayName("DELETE /admin/shops/{id} - Succès")
    void deleteShop_Success() throws Exception {
        Shop shop = new Shop();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));

        mockMvc.perform(delete("/admin/shops/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/shops/{id} - Non trouvé")
    void deleteShop_NotFound() throws Exception {
        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/admin/shops/1"))
                .andExpect(status().isNotFound());
    }
}