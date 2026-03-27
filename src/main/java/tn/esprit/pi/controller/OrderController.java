package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, String> request, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }
        
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.get("shippingAddress"));
        order.setPhoneNumber(request.get("phoneNumber"));
        order.setOrderItems(new HashSet<>());
        
        Order savedOrder = orderRepository.save(order);
        
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setSubtotal(cartItem.getSubtotal());
            orderItemRepository.save(orderItem);
        }
        
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);
        
        return ResponseEntity.ok(Map.of("message", "Order placed successfully", "orderId", savedOrder.getId()));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        return ResponseEntity.ok(orderRepository.findByUserOrderByOrderDateDesc(user));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId, Authentication auth) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    User user = userRepository.findByEmail(auth.getName()).orElse(null);
                    if (user != null && (order.getUser().equals(user) || user.getRole() == Role.ADMIN)) {
                        return ResponseEntity.ok(order);
                    }
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).<Order>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Admin backoffice ──────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @GetMapping("/admin/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        OrderStatus previous = order.getStatus();
        order.setStatus(status);
        orderRepository.save(order);

        if (status == OrderStatus.CONFIRMED && previous != OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(Math.max(0, product.getStock() - item.getQuantity()));
                    productRepository.save(product);
                }
            }
        }

        if (status == OrderStatus.CANCELLED && previous == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/admin/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    orderRepository.delete(order);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getOrderStats() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return ResponseEntity.ok(Map.of(
                "totalOrders",    orderRepository.count(),
                "pending",        orderRepository.countByStatus(OrderStatus.PENDING),
                "confirmed",      orderRepository.countByStatus(OrderStatus.CONFIRMED),
                "processing",     orderRepository.countByStatus(OrderStatus.PROCESSING),
                "shipped",        orderRepository.countByStatus(OrderStatus.SHIPPED),
                "delivered",      orderRepository.countByStatus(OrderStatus.DELIVERED),
                "cancelled",      orderRepository.countByStatus(OrderStatus.CANCELLED),
                "revenueThisMonth", orderRepository.sumRevenueFrom(startOfMonth)
        ));
    }
}
