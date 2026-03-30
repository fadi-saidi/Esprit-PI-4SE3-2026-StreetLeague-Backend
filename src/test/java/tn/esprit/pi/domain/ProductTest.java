package tn.esprit.pi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Football Ball");
        product.setPrice(29.99);
        product.setStock(50);
        product.setCategory("Sports Equipment");
        product.setImage("football.jpg");
        product.setSportType(SportType.FOOTBALL);
        product.setShop(null);                    // Shop can be null in unit test
        product.setCartItems(new HashSet<>());
        product.setReviews(new HashSet<>());
        product.setReviewCount(12);               // transient
        product.setAverageRating(4.5);            // transient
    }

    @Test
    void shouldCreateProductUsingNoArgsConstructorAndSetters() {
        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Football Ball", product.getName());
        assertEquals(29.99, product.getPrice());
        assertEquals(50, product.getStock());
        assertEquals("Sports Equipment", product.getCategory());
        assertEquals("football.jpg", product.getImage());
        assertEquals(SportType.FOOTBALL, product.getSportType());
        assertNull(product.getShop());
        assertNotNull(product.getCartItems());
        assertNotNull(product.getReviews());
    }

    @Test
    void shouldCreateProductWithAllArgsConstructor() {
        Set<CartItem> cartItems = new HashSet<>();
        Set<ProductReview> reviews = new HashSet<>();

        // All 12 parameters required by @AllArgsConstructor
        Product p = new Product(
                2L,                          // id
                "Basketball",                // name
                49.99,                       // price
                30,                          // stock
                "Team Sports",               // category
                "basket.jpg",                // image
                SportType.BASKETBALL,        // sportType
                null,                        // shop
                cartItems,                   // cartItems
                reviews,                     // reviews
                8,                           // reviewCount (transient)
                4.8                          // averageRating (transient)
        );

        assertNotNull(p);
        assertEquals(2L, p.getId());
        assertEquals("Basketball", p.getName());
        assertEquals(49.99, p.getPrice());
        assertEquals(SportType.BASKETBALL, p.getSportType());
        assertEquals(8, p.getReviewCount());
        assertEquals(4.8, p.getAverageRating());
    }

    @Test
    void shouldCalculateSubtotalForCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(4);
        cartItem.setProduct(product);

        // Using Product's price to calculate subtotal
        double expectedSubtotal = cartItem.getQuantity() * product.getPrice();
        cartItem.setSubtotal(expectedSubtotal);

        assertEquals(119.96, cartItem.getSubtotal(), 0.001);
    }

    @Test
    void shouldHandleTransientFields() {
        assertEquals(12, product.getReviewCount());
        assertEquals(4.5, product.getAverageRating());
    }

    @Test
    void shouldAllowNullValues() {
        product.setPrice(null);
        product.setStock(null);
        product.setShop(null);

        assertNull(product.getPrice());
        assertNull(product.getStock());
        assertNull(product.getShop());
    }
}