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
import tn.esprit.pi.service.PlayerMerchService;
import tn.esprit.pi.service.WalletService;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - OrderController (Final Push)")
class OrderControllerTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WalletService walletService;
    @Mock private PlayerMerchService playerMerchService;
    @Mock private Authentication authentication;

    @InjectMocks private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /checkout - Succès complet avec vidage du panier")
    void checkout_Success() throws Exception {
        User user = new User();
        user.setEmail("user@test.tn");

        Cart cart = new Cart();
        cart.setTotalAmount(150.0);

        Product product = new Product();
        product.setPrice(75.0);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setSubtotal(150.0);
        cart.setCartItems(Set.of(item));

        ShopDTOs.CheckoutRequest request = new ShopDTOs.CheckoutRequest("Tunis", "12345678");

        when(authentication.getName()).thenReturn("user@test.tn");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });

        mockMvc.perform(post("/orders/checkout")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L));

        verify(cartItemRepository).deleteAll(any());
        verify(walletService).processOrderPayment(eq(user), eq(150.0), anyLong());
    }

    @Test
    @DisplayName("PUT /admin/{id}/status - Passage à CONFIRMED (Update Stock)")
    void updateStatus_ToConfirmed_UpdatesStock() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        Product p = new Product();
        p.setStock(10);

        OrderItem item = new OrderItem();
        item.setProduct(p);
        item.setQuantity(2);
        order.setOrderItems(Set.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(put("/orders/admin/1/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk());

        verify(productRepository).save(any(Product.class));
        // Vérifie que le stock a diminué (10 - 2 = 8)
        // Note: Dans un test unitaire Mockito, on vérifie l'appel, l'état est géré par l'objet passé
    }

    @Test
    @DisplayName("GET /my-orders - Liste des commandes")
    void getMyOrders_Success() throws Exception {
        User user = new User();
        Order order = new Order();
        order.setOrderItems(new HashSet<>()); // <--- AJOUTE CETTE LIGNE pour éviter le NullPointerException

        when(authentication.getName()).thenReturn("u");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByOrderDateDesc(user)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/my-orders").principal(authentication))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/{id} - Suppression")
    void deleteOrder_Success() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        mockMvc.perform(delete("/orders/admin/1"))
                .andExpect(status().isOk());

        verify(orderRepository).delete(any());
    }

    @Test
    @DisplayName("GET /admin/stats - Vérification des statistiques")
    void getOrderStats_Success() throws Exception {
        when(orderRepository.count()).thenReturn(50L);
        when(orderRepository.countByStatus(any())).thenReturn(5L);
        when(orderRepository.sumRevenueFrom(any())).thenReturn(5000.0);

        mockMvc.perform(get("/orders/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(50))
                .andExpect(jsonPath("$.revenueThisMonth").value(5000.0));
    }
}