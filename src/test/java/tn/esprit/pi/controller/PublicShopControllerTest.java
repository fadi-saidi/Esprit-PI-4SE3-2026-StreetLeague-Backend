package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.domain.Product;
import tn.esprit.pi.domain.Shop;
import tn.esprit.pi.domain.SportType;
import tn.esprit.pi.repository.ProductRepository;
import tn.esprit.pi.repository.ShopRepository;
import tn.esprit.pi.security.jwt.JwtService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicShopController.class)
class PublicShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopRepository shopRepository;
    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private Shop shop;
    private Shop shop2;
    private Product product;
    private Product product2;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        shop = new Shop();
        shop.setId(1L);
        shop.setName("Test Shop");
        shop.setDescription("A test shop");
        shop.setAddress("123 Test Street");
        shop.setContactEmail("test@shop.com");
        shop.setPhoneNumber("+1234567890");

        shop2 = new Shop();
        shop2.setId(2L);
        shop2.setName("Second Shop");
        shop2.setDescription("Another test shop");
        shop2.setAddress("456 Second Street");
        shop2.setContactEmail("second@shop.com");
        shop2.setPhoneNumber("+0987654321");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setSportType(SportType.FOOTBALL);
        product.setShop(shop);
        product.setStock(10);
        product.setCategory("Equipment");
        product.setImage("product1.jpg");

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Second Product");
        product2.setPrice(75.0);
        product2.setSportType(SportType.BASKETBALL);
        product2.setShop(shop);
        product2.setStock(5);
        product2.setCategory("Apparel");
        product2.setImage("product2.jpg");
    }

    @Test
    void getAllShops_ShouldReturnAllShops() throws Exception {
        when(shopRepository.findAll()).thenReturn(List.of(shop, shop2));

        mockMvc.perform(get("/shops")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Test Shop"))
                .andExpect(jsonPath("$[0].description").value("A test shop"))
                .andExpect(jsonPath("$[0].address").value("123 Test Street"))
                .andExpect(jsonPath("$[0].contactEmail").value("test@shop.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$[1].name").value("Second Shop"))
                .andExpect(jsonPath("$[1].description").value("Another test shop"));

        verify(shopRepository).findAll();
    }

    @Test
    void getAllShops_ShouldReturnEmptyListWhenNoShops() throws Exception {
        when(shopRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/shops")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(shopRepository).findAll();
    }

    @Test
    void getShopById_ShouldReturnShop() throws Exception {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));

        mockMvc.perform(get("/shops/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Shop"))
                .andExpect(jsonPath("$.description").value("A test shop"))
                .andExpect(jsonPath("$.address").value("123 Test Street"))
                .andExpect(jsonPath("$.contactEmail").value("test@shop.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(shopRepository).findById(1L);
    }

    @Test
    void getShopById_ShouldReturnNotFound() throws Exception {
        when(shopRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/shops/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(shopRepository).findById(999L);
    }

    @Test
    void getShopById_ShouldHandleNullFields() throws Exception {
        Shop shopWithNulls = new Shop();
        shopWithNulls.setId(3L);
        shopWithNulls.setName("Minimal Shop");
        // Other fields are null

        when(shopRepository.findById(3L)).thenReturn(Optional.of(shopWithNulls));

        mockMvc.perform(get("/shops/3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Minimal Shop"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.address").isEmpty())
                .andExpect(jsonPath("$.contactEmail").isEmpty())
                .andExpect(jsonPath("$.phoneNumber").isEmpty());

        verify(shopRepository).findById(3L);
    }

    @Test
    void getShopById_ShouldHandleInvalidIdFormat() throws Exception {
        mockMvc.perform(get("/shops/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(shopRepository, never()).findById(any());
    }

    @Test
    void getShopById_ShouldHandleNegativeId() throws Exception {
        when(shopRepository.findById(-1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/shops/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(shopRepository).findById(-1L);
    }

    @Test
    void getShopById_ShouldHandleZeroId() throws Exception {
        when(shopRepository.findById(0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/shops/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(shopRepository).findById(0L);
    }
}
