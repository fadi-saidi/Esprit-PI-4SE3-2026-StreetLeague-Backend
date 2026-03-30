package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import tn.esprit.pi.domain.MerchStatus;
import tn.esprit.pi.domain.OrderStatus;
import tn.esprit.pi.domain.SportType;

import java.time.LocalDateTime;
import java.util.List;

public class ShopDTOs {

    // ─── Product ──────────────────────────────────────────────────────────────

    public record ProductRequest(
            @NotBlank(message = "Name is required")
            @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
            String name,

            @NotNull(message = "Price is required")
            @Positive(message = "Price must be positive")
            Double price,

            @NotNull(message = "Stock is required")
            @PositiveOrZero(message = "Stock cannot be negative")
            Integer stock,

            @NotBlank(message = "Category is required")
            String category,

            String image,

            @NotNull(message = "Sport type is required")
            SportType sportType
    ) {}

    public record ProductResponse(
            Long id,
            String name,
            Double price,
            Integer stock,
            String category,
            String image,
            SportType sportType,
            Integer reviewCount,
            Double averageRating
    ) {}

    // ─── Review ───────────────────────────────────────────────────────────────

    public record ReviewRequest(
            @NotNull(message = "Rating is required")
            @Min(value = 1, message = "Rating must be at least 1")
            @Max(value = 5, message = "Rating must be at most 5")
            Integer rating,

            @NotBlank(message = "Comment is required")
            @Size(max = 1000, message = "Comment must not exceed 1000 characters")
            String comment
    ) {}

    public record ReviewResponse(
            Long id,
            Integer rating,
            String comment,
            String createdAt,
            Long userId,
            String username
    ) {}

    // ─── Cart ─────────────────────────────────────────────────────────────────

    public record AddToCartRequest(
            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 100, message = "Quantity cannot exceed 100")
            Integer quantity
    ) {}

    public record UpdateCartItemRequest(
            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 100, message = "Quantity cannot exceed 100")
            Integer quantity
    ) {}

    public record CartItemResponse(
            Long id,
            Integer quantity,
            Double subtotal,
            ProductResponse product
    ) {}

    public record CartResponse(
            Long id,
            Double totalAmount,
            List<CartItemResponse> cartItems
    ) {}

    // ─── Order ────────────────────────────────────────────────────────────────

    public record CheckoutRequest(
            @NotBlank(message = "Shipping address is required")
            @Size(max = 255, message = "Shipping address must not exceed 255 characters")
            String shippingAddress,

            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Invalid phone number")
            String phoneNumber
    ) {}

    public record OrderItemResponse(
            Long id,
            Integer quantity,
            Double price,
            Double subtotal,
            ProductResponse product
    ) {}

    public record OrderResponse(
            Long id,
            LocalDateTime orderDate,
            Double totalAmount,
            OrderStatus status,
            String shippingAddress,
            String phoneNumber,
            List<OrderItemResponse> orderItems
    ) {}

    public record AdminOrderResponse(
            Long id,
            LocalDateTime orderDate,
            Double totalAmount,
            OrderStatus status,
            String shippingAddress,
            String phoneNumber,
            Long userId,
            String username,
            String userEmail,
            List<OrderItemResponse> orderItems
    ) {}

    public record CheckoutResponse(
            String message,
            Long orderId
    ) {}

    // ─── Player Merch ─────────────────────────────────────────────────────────

    public record PlayerMerchRequest(
            @NotBlank(message = "Name is required")
            @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
            String name,

            @Size(max = 500, message = "Description must not exceed 500 characters")
            String description,

            @NotNull(message = "Price is required")
            @Positive(message = "Price must be positive")
            Double price,

            @NotNull(message = "Stock is required")
            @PositiveOrZero(message = "Stock cannot be negative")
            Integer stock,

            @NotBlank(message = "Category is required")
            String category,

            String image,

            @NotNull(message = "Sport type is required")
            SportType sportType
    ) {}

    public record PlayerMerchResponse(
            Long id,
            String name,
            String description,
            Double price,
            Integer stock,
            String category,
            String image,
            SportType sportType,
            MerchStatus status,
            String sellerName,
            Long sellerId,
            String submittedAt,
            String approvedAt,
            String rejectionReason
    ) {}

    public record MerchApprovalRequest(
            @NotBlank(message = "Reason is required for rejection")
            String reason
    ) {}
}
