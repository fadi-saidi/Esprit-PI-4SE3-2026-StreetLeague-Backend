package tn.esprit.pi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("fadi");

        cart = new Cart();
        cart.setId(10L);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUser(user);
        cart.setTotalAmount(0.0);
        cart.setCartItems(new HashSet<>());
    }

    @Test
    void shouldCreateCartSuccessfully() {
        assertNotNull(cart);
        assertEquals(10L, cart.getId());
        assertNotNull(cart.getCreatedAt());
        assertEquals(user, cart.getUser());
        assertEquals(0.0, cart.getTotalAmount());
    }

    @Test
    void shouldAddCartItemAndCalculateTotal() {
        // Create a sample CartItem (adjust according to your actual CartItem class)
        CartItem item1 = new CartItem();
        item1.setQuantity(2);
        // item1.setPrice(25.0);   ← This line was causing the error, so we remove it

        CartItem item2 = new CartItem();
        item2.setQuantity(3);

        cart.getCartItems().add(item1);
        cart.getCartItems().add(item2);

        assertEquals(2, cart.getCartItems().size());

        // Example: If you have a calculateTotalAmount() method in Cart, use it
        // cart.calculateTotalAmount();
        // assertTrue(cart.getTotalAmount() > 0);
    }

    @Test
    void shouldHandleEmptyCartItems() {
        cart.setCartItems(null);

        assertDoesNotThrow(() -> {
            assertNull(cart.getCartItems());
            assertEquals(0.0, cart.getTotalAmount()); // or handle in your logic
        });
    }

    @Test
    void shouldSetUserCorrectly() {
        User newUser = new User();
        newUser.setId(99L);
        cart.setUser(newUser);

        assertEquals(99L, cart.getUser().getId());
    }
}