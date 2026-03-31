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
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.security.jwt.JwtService;
import tn.esprit.pi.service.WalletService;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private OrderItemRepository orderItemRepository;
    @MockitoBean
    private CartRepository cartRepository;
    @MockitoBean
    private CartItemRepository cartItemRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private WalletService walletService;
    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private User user;
    private User adminUser;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private Order order;
    private OrderItem orderItem;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setRole(Role.PLAYER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setPoints(1000);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(29.99);
        product.setStock(10);
        product.setCategory("Sports");
        product.setSportType(SportType.FOOTBALL);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setSubtotal(59.98);

        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(cartItem);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setTotalAmount(59.98);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setCartItems(cartItems);

        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(29.99);
        orderItem.setSubtotal(59.98);

        Set<OrderItem> orderItems = new HashSet<>();
        orderItems.add(orderItem);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(59.98);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Test St");
        order.setPhoneNumber("+216 12 345 678");
        order.setOrderItems(orderItems);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void checkout_ShouldCreateOrderSuccessfully() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Test St", "+216 12 345 678");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        doNothing().when(walletService).processOrderPayment(eq(user), eq(59.98), eq(1L));

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.orderId").value(1L));

        verify(walletService).processOrderPayment(eq(user), eq(59.98), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void checkout_ShouldReturnErrorWhenCartEmpty() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Test St", "+216 12 345 678");
        Cart emptyCart = new Cart();
        emptyCart.setCartItems(new HashSet<>());
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(emptyCart));

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cart is empty"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void checkout_ShouldReturnErrorWhenInsufficientStock() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Test St", "+216 12 345 678");
        product.setStock(1); // Less than cart quantity (2)
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient stock for product: Test Product"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void checkout_ShouldHandleWalletInsufficientFunds() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Test St", "+216 12 345 678");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        doThrow(new RuntimeException("Insufficient wallet balance"))
                .when(walletService).processOrderPayment(eq(user), eq(59.98), any());

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient wallet balance"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMyOrders_ShouldReturnOrderList() throws Exception {
        List<Order> orders = Arrays.asList(order);
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByOrderDateDesc(user)).thenReturn(orders);

        mockMvc.perform(get("/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].totalAmount").value(59.98))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderById_ShouldReturnOrderDetails() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(59.98))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.shippingAddress").value("123 Test St"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderById_ShouldReturnNotFound() throws Exception {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderById_ShouldReturnForbiddenForOtherUsersOrder() throws Exception {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        order.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void getAllOrders_ShouldReturnAllOrdersForAdmin() throws Exception {
        List<Order> orders = Arrays.asList(order);
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(orders);

        mockMvc.perform(get("/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userInfo.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllOrders_ShouldReturnForbiddenForNonAdmin() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/orders/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(put("/orders/admin/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order status updated successfully"));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void cancelOrder_ShouldCancelOrderAndRefund() throws Exception {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doNothing().when(walletService).processOrderRefund(eq(user), eq(59.98), eq(1L));

        mockMvc.perform(put("/orders/admin/1/cancel")
                        .param("reason", "Customer request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

        verify(walletService).processOrderRefund(eq(user), eq(59.98), eq(1L));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void getOrderStats_ShouldReturnStatistics() throws Exception {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.SHIPPED)).thenReturn(10L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(15L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(2L);
        when(orderRepository.getTotalRevenue()).thenReturn(1500.0);

        mockMvc.perform(get("/orders/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(5))
                .andExpect(jsonPath("$.shipped").value(10))
                .andExpect(jsonPath("$.delivered").value(15))
                .andExpect(jsonPath("$.cancelled").value(2))
                .andExpect(jsonPath("$.totalRevenue").value(1500.0));
    }

    @Test
    void checkout_ShouldReturnUnauthorizedWithoutAuth() throws Exception {
        CheckoutRequest request = new CheckoutRequest("123 Test St", "+216 12 345 678");

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void checkout_ShouldReturnBadRequestForInvalidRequest() throws Exception {
        CheckoutRequest request = new CheckoutRequest("", ""); // Invalid empty fields

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}