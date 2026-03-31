package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.domain.Shop;
import tn.esprit.pi.repository.ShopRepository;
import tn.esprit.pi.security.jwt.JwtService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopRepository shopRepository;
    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private Shop shop;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        shop = new Shop();
        shop.setId(1L);
        shop.setName("Test Shop");
        shop.setDescription("A test shop for sports equipment");
        shop.setAddress("123 Test Street");
        shop.setContactEmail("test@shop.com");
        shop.setPhoneNumber("+1234567890");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllShops_ShouldReturnAllShops() throws Exception {
        Shop shop2 = new Shop();
        shop2.setId(2L);
        shop2.setName("Second Shop");
        shop2.setDescription("Another test shop");

        when(shopRepository.findAll()).thenReturn(List.of(shop, shop2));

        mockMvc.perform(get("/admin/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Test Shop"))
                .andExpect(jsonPath("$[1].name").value("Second Shop"));

        verify(shopRepository).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getShopById_ShouldReturnShop() throws Exception {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));

        mockMvc.perform(get("/admin/shops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Shop"))
                .andExpect(jsonPath("$.description").value("A test shop for sports equipment"))
                .andExpect(jsonPath("$.address").value("123 Test Street"))
                .andExpect(jsonPath("$.contactEmail").value("test@shop.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(shopRepository).findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getShopById_ShouldReturnNotFound() throws Exception {
        when(shopRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/shops/999"))
                .andExpect(status().isNotFound());

        verify(shopRepository).findById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShop_ShouldCreateAndReturnShop() throws Exception {
        Shop newShop = new Shop();
        newShop.setName("New Shop");
        newShop.setDescription("Brand new shop");
        newShop.setAddress("456 New Street");
        newShop.setContactEmail("new@shop.com");
        newShop.setPhoneNumber("+9876543210");

        Shop savedShop = new Shop();
        savedShop.setId(2L);
        savedShop.setName("New Shop");
        savedShop.setDescription("Brand new shop");
        savedShop.setAddress("456 New Street");
        savedShop.setContactEmail("new@shop.com");
        savedShop.setPhoneNumber("+9876543210");

        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);

        mockMvc.perform(post("/admin/shops")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newShop)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("New Shop"))
                .andExpect(jsonPath("$.description").value("Brand new shop"))
                .andExpect(jsonPath("$.address").value("456 New Street"))
                .andExpect(jsonPath("$.contactEmail").value("new@shop.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        verify(shopRepository).save(any(Shop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShop_ShouldUpdateAndReturnShop() throws Exception {
        Shop updatedShopDetails = new Shop();
        updatedShopDetails.setName("Updated Shop");
        updatedShopDetails.setDescription("Updated description");
        updatedShopDetails.setAddress("789 Updated Street");
        updatedShopDetails.setContactEmail("updated@shop.com");
        updatedShopDetails.setPhoneNumber("+1111111111");

        Shop updatedShop = new Shop();
        updatedShop.setId(1L);
        updatedShop.setName("Updated Shop");
        updatedShop.setDescription("Updated description");
        updatedShop.setAddress("789 Updated Street");
        updatedShop.setContactEmail("updated@shop.com");
        updatedShop.setPhoneNumber("+1111111111");

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(shopRepository.save(any(Shop.class))).thenReturn(updatedShop);

        mockMvc.perform(put("/admin/shops/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedShopDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Shop"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.address").value("789 Updated Street"))
                .andExpect(jsonPath("$.contactEmail").value("updated@shop.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1111111111"));

        verify(shopRepository).findById(1L);
        verify(shopRepository).save(any(Shop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShop_ShouldReturnNotFoundForInvalidId() throws Exception {
        Shop updatedShopDetails = new Shop();
        updatedShopDetails.setName("Updated Shop");

        when(shopRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/shops/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedShopDetails)))
                .andExpect(status().isNotFound());

        verify(shopRepository).findById(999L);
        verify(shopRepository, never()).save(any(Shop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShop_ShouldDeleteShop() throws Exception {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        doNothing().when(shopRepository).delete(shop);

        mockMvc.perform(delete("/admin/shops/1"))
                .andExpect(status().isOk());

        verify(shopRepository).findById(1L);
        verify(shopRepository).delete(shop);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShop_ShouldReturnNotFoundForInvalidId() throws Exception {
        when(shopRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/admin/shops/999"))
                .andExpect(status().isNotFound());

        verify(shopRepository).findById(999L);
        verify(shopRepository, never()).delete(any(Shop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShop_ShouldHandleEmptyFields() throws Exception {
        Shop newShop = new Shop();
        newShop.setName("Minimal Shop");
        // Only setting name, other fields are null

        Shop savedShop = new Shop();
        savedShop.setId(3L);
        savedShop.setName("Minimal Shop");

        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);

        mockMvc.perform(post("/admin/shops")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newShop)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Minimal Shop"));

        verify(shopRepository).save(any(Shop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShop_ShouldUpdateOnlyProvidedFields() throws Exception {
        Shop partialUpdate = new Shop();
        partialUpdate.setName("Partially Updated Shop");
        partialUpdate.setDescription("New description only");
        // Not setting other fields

        Shop existingShop = new Shop();
        existingShop.setId(1L);
        existingShop.setName("Old Name");
        existingShop.setDescription("Old description");
        existingShop.setAddress("Old Address");
        existingShop.setContactEmail("old@shop.com");
        existingShop.setPhoneNumber("+0000000000");

        when(shopRepository.findById(1L)).thenReturn(Optional.of(existingShop));
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> {
            Shop shopToSave = invocation.getArgument(0);
            shopToSave.setId(1L);
            return shopToSave;
        });

        mockMvc.perform(put("/admin/shops/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Partially Updated Shop"))
                .andExpect(jsonPath("$.description").value("New description only"));

        verify(shopRepository).findById(1L);
        verify(shopRepository).save(any(Shop.class));
    }
}