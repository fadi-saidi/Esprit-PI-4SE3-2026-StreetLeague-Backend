package tn.esprit.pi.dto;

import org.junit.jupiter.api.Test;
import tn.esprit.pi.domain.OrderStatus;
import tn.esprit.pi.domain.SportType;
import tn.esprit.pi.dto.ShopDTOs.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ShopDTOsTest {

    @Test
    void productRequest_ShouldCreateWithAllFields() {
        // Act
        ProductRequest request = new ProductRequest(
                "Football Jersey",
                29.99,
                100,
                "Apparel",
                "jersey.jpg",
                SportType.FOOTBALL
        );

        // Assert
        assertEquals("Football Jersey", request.name());
        assertEquals(29.99, request.price());
        assertEquals(100, request.stock());
        assertEquals("Apparel", request.category());
        assertEquals("jersey.jpg", request.image());
        assertEquals(SportType.FOOTBALL, request.sportType());
    }

    @Test
    void productResponse_ShouldCreateWithAllFields() {
        // Act
        ProductResponse response = new ProductResponse(
                1L,
                "Football Jersey",
                29.99,
                100,
                "Apparel",
                "jersey.jpg",
                SportType.FOOTBALL,
                5,
                4.5
        );

        // Assert
        assertEquals(1L, response.id());
        assertEquals("Football Jersey", response.name());
        assertEquals(29.99, response.price());
        assertEquals(100, response.stock());
        assertEquals("Apparel", response.category());
        assertEquals(SportType.FOOTBALL, response.sportType());
        assertEquals(5, response.reviewCount());
        assertEquals(4.5, response.averageRating());
    }

    @Test
    void reviewRequest_ShouldCreateWithRatingAndComment() {
        // Act
        ReviewRequest request = new ReviewRequest(5, "Excellent product!");

        // Assert
        assertEquals(5, request.rating());
        assertEquals("Excellent product!", request.comment());
    }

    @Test
    void reviewResponse_ShouldCreateWithAllFields() {
        // Act
        ReviewResponse response = new ReviewResponse(
                1L,
                5,
                "Great product!",
                "2024-01-15T10:30:00",
                1L,
                "John Doe"
        );

        // Assert
        assertEquals(1L, response.id());
        assertEquals(5, response.rating());
        assertEquals("Great product!", response.comment());
        assertEquals("2024-01-15T10:30:00", response.createdAt());
        assertEquals(1L, response.userId());
        assertEquals("John Doe", response.username());
    }

    @Test
    void addToCartRequest_ShouldCreateWithQuantity() {
        // Act
        AddToCartRequest request = new AddToCartRequest(3);

        // Assert
        assertEquals(3, request.quantity());
    }

    @Test
    void updateCartItemRequest_ShouldCreateWithQuantity() {
        // Act
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        // Assert
        assertEquals(5, request.quantity());
    }

    @Test
    void cartItemResponse_ShouldCreateWithAllFields() {
        // Act
        ProductResponse product = new ProductResponse(1L, "Jersey", 29.99, 10, "Apparel", "jersey.jpg", SportType.FOOTBALL, null, null);
        CartItemResponse response = new CartItemResponse(1L, 2, 59.98, product);

        // Assert
        assertEquals(1L, response.id());
        assertEquals(2, response.quantity());
        assertEquals(59.98, response.subtotal());
        assertEquals(product, response.product());
    }

    @Test
    void cartResponse_ShouldCreateWithAllFields() {
        // Act
        ProductResponse product = new ProductResponse(1L, "Jersey", 29.99, 10, "Apparel", "jersey.jpg", SportType.FOOTBALL, null, null);
        CartItemResponse cartItem = new CartItemResponse(1L, 2, 59.98, product);
        CartResponse response = new CartResponse(1L, 59.98, Arrays.asList(cartItem));

        // Assert
        assertEquals(1L, response.id());
        assertEquals(59.98, response.totalAmount());
        assertEquals(1, response.cartItems().size());
        assertEquals(cartItem, response.cartItems().get(0));
    }

    @Test
    void checkoutRequest_ShouldCreateWithAddressAndPhone() {
        // Act
        CheckoutRequest request = new CheckoutRequest("123 Main St, City", "+216 12 345 678");

        // Assert
        assertEquals("123 Main St, City", request.shippingAddress());
        assertEquals("+216 12 345 678", request.phoneNumber());
    }

    @Test
    void orderItemResponse_ShouldCreateWithAllFields() {
        // Act
        ProductResponse product = new ProductResponse(1L, "Jersey", 29.99, 10, "Apparel", "jersey.jpg", SportType.FOOTBALL, null, null);
        OrderItemResponse response = new OrderItemResponse(1L, 2, 29.99, 59.98, product);

        // Assert
        assertEquals(1L, response.id());
        assertEquals(2, response.quantity());
        assertEquals(29.99, response.price());
        assertEquals(59.98, response.subtotal());
        assertEquals(product, response.product());
    }

    @Test
    void orderResponse_ShouldCreateWithAllFields() {
        // Act
        LocalDateTime orderDate = LocalDateTime.now();
        ProductResponse product = new ProductResponse(1L, "Jersey", 29.99, 10, "Apparel", "jersey.jpg", SportType.FOOTBALL, null, null);
        OrderItemResponse orderItem = new OrderItemResponse(1L, 2, 29.99, 59.98, product);
        
        OrderResponse response = new OrderResponse(
                1L,
                orderDate,
                59.98,
                OrderStatus.PENDING,
                "123 Main St",
                "+216 12 345 678",
                Arrays.asList(orderItem)
        );

        // Assert
        assertEquals(1L, response.id());
        assertEquals(orderDate, response.orderDate());
        assertEquals(59.98, response.totalAmount());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals("123 Main St", response.shippingAddress());
        assertEquals("+216 12 345 678", response.phoneNumber());
        assertEquals(1, response.orderItems().size());
    }

    @Test
    void checkoutResponse_ShouldCreateWithMessageAndOrderId() {
        // Act
        CheckoutResponse response = new CheckoutResponse("Order placed successfully", 123L);

        // Assert
        assertEquals("Order placed successfully", response.message());
        assertEquals(123L, response.orderId());
    }
}