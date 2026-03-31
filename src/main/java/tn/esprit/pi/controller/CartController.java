package tn.esprit.pi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ShopDTOs.*;
import tn.esprit.pi.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setCreatedAt(LocalDateTime.now());
            c.setTotalAmount(0.0);
            c.setCartItems(new HashSet<>());
            return cartRepository.save(c);
        });
    }

    private void updateCartTotal(Cart cart) {
        double total = cart.getCartItems().stream().mapToDouble(CartItem::getSubtotal).sum();
        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        Product p = item.getProduct();
        ProductResponse pr = new ProductResponse(
                p.getId(), p.getName(), p.getPrice(), p.getStock(),
                p.getCategory(), p.getImage(), p.getSportType(), null, null
        );
        return new CartItemResponse(item.getId(), item.getQuantity(), item.getSubtotal(), pr);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toCartItemResponse).toList();
        return new CartResponse(cart.getId(), cart.getTotalAmount(), items);
    }

    // ─── Endpoints ────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Authentication auth) {
        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toCartResponse(getOrCreateCart(user)));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId,
                                       @RequestBody(required = false) AddToCartRequest request,
                                       @RequestParam(defaultValue = "1") Integer quantity,
                                       Authentication auth) {
        try {
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
            }

            if (request.quantity() <= 0 || request.quantity() > 100) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid quantity. Must be between 1 and 100"));
            }

            if (product.getStock() == null || product.getStock() < request.quantity()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Insufficient stock. Available: " + (product.getStock() != null ? product.getStock() : 0)));
            }

            Cart cart = getOrCreateCart(user);
            CartItem existing = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

            if (existing != null) {
                int newQuantity = existing.getQuantity() + request.quantity();
                if (newQuantity > product.getStock()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cannot add more items. Total would exceed available stock"));
                }
                existing.setQuantity(newQuantity);
                existing.setSubtotal(newQuantity * product.getPrice());
                cartItemRepository.save(existing);
            } else {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(request.quantity());
                item.setSubtotal(request.quantity() * product.getPrice());
                cartItemRepository.save(item);
            }

            updateCartTotal(cart);
            return ResponseEntity.ok(Map.of(
                "message", "Product added to cart successfully",
                "productName", product.getName(),
                "quantity", request.quantity()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to add product to cart: " + e.getMessage()));
        }
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId,
                                            @Valid @RequestBody UpdateCartItemRequest request,
                                            Authentication auth) {
        CartItem item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (!item.getCart().getUser().equals(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        item.setQuantity(request.quantity());
        item.setSubtotal(request.quantity() * item.getProduct().getPrice());
        cartItemRepository.save(item);
        updateCartTotal(item.getCart());
        return ResponseEntity.ok(Map.of("message", "Cart updated"));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId, Authentication auth) {
        CartItem item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (!item.getCart().getUser().equals(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Cart cart = item.getCart();
        cartItemRepository.delete(item);
        updateCartTotal(cart);
        return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) return ResponseEntity.notFound().build();

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}
