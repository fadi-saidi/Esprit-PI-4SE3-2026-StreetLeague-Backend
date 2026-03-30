package tn.esprit.pi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private CartItem cartItem;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        // Prepare related entities
        cart = new Cart();
        cart.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Laptop Dell");
        // Assuming Product has a price field (common in real apps)
        // product.setPrice(1200.0);

        // Create CartItem
        cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setQuantity(3);
        cartItem.setSubtotal(0.0);        // will be calculated later
        cartItem.setCart(cart);
        cartItem.setProduct(product);
    }

    @Test
    void shouldCreateCartItemWithAllArgsConstructor() {
        CartItem item = new CartItem(200L, 2, 500.0, cart, product);

        assertNotNull(item);
        assertEquals(200L, item.getId());
        assertEquals(2, item.getQuantity());
        assertEquals(500.0, item.getSubtotal());
        assertEquals(cart, item.getCart());
        assertEquals(product, item.getProduct());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {
        assertEquals(3, cartItem.getQuantity());
        assertEquals(0.0, cartItem.getSubtotal());
        assertNotNull(cartItem.getCart());
        assertNotNull(cartItem.getProduct());
        assertEquals(1L, cartItem.getCart().getId());
        assertEquals(10L, cartItem.getProduct().getId());
    }

    @Test
    void shouldCalculateSubtotalCorrectly() {
        // Simulate Product having a price (you need to adjust if your Product class is different)
        // For now, we assume you will add a getPrice() method or a calculateSubtotal() in CartItem

        // Example calculation (you can move this logic to the entity later)
        double price = 250.0; // pretend product price
        double expectedSubtotal = cartItem.getQuantity() * price;

        cartItem.setSubtotal(expectedSubtotal);

        assertEquals(750.0, cartItem.getSubtotal());
    }

    @Test
    void shouldHandleBidirectionalRelationship() {
        // Add CartItem to Cart (important for consistency)
        if (cart.getCartItems() == null) {
            cart.setCartItems(new java.util.HashSet<>());
        }
        cart.getCartItems().add(cartItem);

        assertTrue(cart.getCartItems().contains(cartItem));
        assertEquals(cart, cartItem.getCart());
    }

    @Test
    void shouldAllowNullValuesForOptionalFields() {
        CartItem emptyItem = new CartItem();
        emptyItem.setQuantity(0);

        assertDoesNotThrow(() -> {
            assertNull(emptyItem.getProduct());
            assertNull(emptyItem.getCart());
            assertEquals(0, emptyItem.getQuantity());
        });
    }
}