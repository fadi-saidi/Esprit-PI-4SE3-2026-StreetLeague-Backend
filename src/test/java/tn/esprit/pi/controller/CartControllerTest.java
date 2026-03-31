package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs;
import tn.esprit.pi.repository.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Objectif 90% - CartController")
class CartControllerTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        user = new User();
        user.setId(1L);
        user.setEmail("test@esprit.tn");

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setCartItems(new HashSet<>());
    }

    @Test
    @DisplayName("POST /add/{id} - Succès (Nouveau produit)")
    void addToCart_NewProduct_Success() throws Exception {
        Product p = new Product();
        p.setId(99L);
        p.setPrice(50.0);
        p.setStock(10);
        p.setName("Maillot");

        ShopDTOs.AddToCartRequest req = new ShopDTOs.AddToCartRequest(2);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.of(p));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        mockMvc.perform(post("/cart/add/99")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product added to cart successfully"));

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("POST /add/{id} - Erreur Stock Insuffisant")
    void addToCart_InsufficientStock() throws Exception {
        Product p = new Product();
        p.setStock(1); // Seulement 1 en stock

        ShopDTOs.AddToCartRequest req = new ShopDTOs.AddToCartRequest(5); // Demande 5

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(post("/cart/add/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Insufficient stock")));
    }

    @Test
    @DisplayName("PUT /update/{itemId} - Succès")
    void updateCartItem_Success() throws Exception {
        Product p = new Product();
        p.setPrice(10.0);

        CartItem item = new CartItem();
        item.setId(5L);
        item.setCart(cart);
        item.setProduct(p);

        ShopDTOs.UpdateCartItemRequest req = new ShopDTOs.UpdateCartItemRequest(3);

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(put("/cart/update/5")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /remove/{itemId} - Forbidden (Autre utilisateur)")
    void removeFromCart_Forbidden() throws Exception {
        User otherUser = new User();
        otherUser.setEmail("other@test.tn");

        Cart otherCart = new Cart();
        otherCart.setUser(otherUser);

        CartItem item = new CartItem();
        item.setCart(otherCart);

        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/cart/remove/10").principal(authentication))
                .andExpect(status().isForbidden());
    }
}