# Sponsorship & Shop Module Enhancements

## Summary of Changes

This implementation adds the recommended must-have and nice-to-have endpoints for both Sponsorship and Shop modules based on the priority requirements.

---

## 🎯 Sponsorship Module - New Endpoints

### 1. GET /sponsorships/{id}
**Purpose**: View details of a specific sponsorship  
**Auth**: Public or Authenticated  
**Priority**: Must-have ✅  
**Response**: Single Sponsorship object with all details

### 2. GET /sponsorships/team/{teamId}
**Purpose**: List all sponsorships for a specific team  
**Auth**: Public  
**Priority**: Must-have ✅  
**Use Case**: Team captains and public pages can view team sponsors

### 3. GET /sponsorships/event/{eventId}
**Purpose**: List sponsorships for a specific event/tournament  
**Auth**: Public  
**Priority**: Must-have ✅  
**Use Case**: Event organizers can display event sponsors

### 4. GET /sponsorships/venue/{venueId}
**Purpose**: List sponsorships for a specific venue  
**Auth**: Public  
**Priority**: Nice-to-have ✅  
**Use Case**: Venue owners can see their venue sponsors

### 5. PUT /sponsorships/{id}/renew
**Purpose**: Renew an expiring sponsorship (extend endDate)  
**Auth**: SPONSOR (owner only)  
**Priority**: Nice-to-have ✅  
**Parameters**: `months` (Integer) - number of months to extend  
**Response**: Updated Sponsorship object

### 6. POST /sponsorships/{id}/payment-proof
**Purpose**: Upload proof of payment (critical for Tunisia context)  
**Auth**: SPONSOR (owner only)  
**Priority**: Must-have ✅  
**Parameters**: `proofUrl` (String) - URL/path to payment proof image/PDF  
**Response**: Success message  
**Note**: This is critical for building trust in the Tunisian market

### 7. GET /sponsorships/admin/stats
**Purpose**: Admin dashboard statistics  
**Auth**: ADMIN only  
**Priority**: Nice-to-have ✅  
**Response**:
```json
{
  "totalActive": 15,
  "totalAmountThisMonth": 25000.0,
  "totalPending": 3
}
```

---

## 🛒 Shop Module - New Endpoints

### Products (Already Implemented)
- ✅ GET /products - List all products (public)
- ✅ GET /products/{id} - Product detail (public)
- ✅ GET /products/search - Search & filter (public)
- ✅ GET /products/price-range - Filter by price (public)

### Cart (Already Implemented)
- ✅ GET /cart - View current cart
- ✅ POST /cart/add/{productId} - Add to cart
- ✅ PUT /cart/update/{itemId} - Update quantity
- ✅ DELETE /cart/remove/{itemId} - Remove item
- ✅ DELETE /cart/clear - Clear cart

### Orders (NEW)

#### 1. POST /orders/checkout
**Purpose**: Create order from cart  
**Auth**: Authenticated  
**Priority**: Must-have ✅  
**Request Body**:
```json
{
  "shippingAddress": "123 Main St, Tunis",
  "phoneNumber": "+216 12 345 678"
}
```
**Response**:
```json
{
  "message": "Order placed successfully",
  "orderId": 42
}
```
**Behavior**: 
- Creates order from cart items
- Clears cart after successful checkout
- Sets order status to PENDING

#### 2. GET /orders/my-orders
**Purpose**: List user's past orders  
**Auth**: Authenticated  
**Priority**: Must-have ✅  
**Response**: List of Order objects sorted by date (newest first)

#### 3. GET /orders/{orderId}
**Purpose**: View order details and status  
**Auth**: Authenticated (owner only)  
**Priority**: Must-have ✅  
**Response**: Order object with all items and status

### Product Reviews (NEW)

#### 4. POST /products/{id}/reviews
**Purpose**: Add rating/review after purchase  
**Auth**: Authenticated  
**Priority**: Nice-to-have ✅  
**Request Body**:
```json
{
  "rating": 5,
  "comment": "Great product, fast delivery!"
}
```

#### 5. GET /products/{id}/reviews
**Purpose**: View all reviews for a product  
**Auth**: Public  
**Priority**: Nice-to-have ✅  
**Response**: List of ProductReview objects sorted by date

### Admin Features (NEW)

#### 6. GET /products/low-stock
**Purpose**: Admin alert for products with low stock  
**Auth**: ADMIN only  
**Priority**: Nice-to-have ✅  
**Parameters**: `threshold` (default: 5)  
**Response**: List of products with stock below threshold

---

## 📊 New Database Entities

### 1. Order
```java
- id: Long
- orderDate: LocalDateTime
- totalAmount: Double
- status: OrderStatus (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- shippingAddress: String
- phoneNumber: String
- user: User (ManyToOne)
- orderItems: Set<OrderItem> (OneToMany)
```

### 2. OrderItem
```java
- id: Long
- quantity: Integer
- price: Double (snapshot at order time)
- subtotal: Double
- order: Order (ManyToOne)
- product: Product (ManyToOne)
```

### 3. ProductReview
```java
- id: Long
- rating: Integer (1-5)
- comment: String
- createdAt: LocalDateTime
- product: Product (ManyToOne)
- user: User (ManyToOne)
```

### 4. Sponsorship (Updated)
**New Field**: `paymentProof: String` - URL/path to payment proof document

---

## 🔐 Security Configuration Updates

### Public Endpoints (No Auth Required)
- `/sponsorships/{id}` - View sponsorship details
- `/sponsorships/team/**` - Team sponsorships
- `/sponsorships/event/**` - Event sponsorships
- `/sponsorships/venue/**` - Venue sponsorships
- `/products/**` - All product endpoints (including reviews)

### Authenticated Endpoints
- `/cart/**` - Cart management
- `/orders/**` - Order management

### Role-Based Endpoints
- `/sponsorships/*/renew` - SPONSOR only
- `/sponsorships/*/payment-proof` - SPONSOR only
- `/sponsorships/admin/**` - ADMIN only
- `/products/low-stock` - ADMIN only

---

## 🚀 Usage Examples

### Sponsorship Flow
1. Sponsor submits request: `POST /sponsorships/submit`
2. Admin approves: `PUT /sponsorships/admin/{id}/approve`
3. Sponsor uploads payment proof: `POST /sponsorships/{id}/payment-proof`
4. Public views team sponsors: `GET /sponsorships/team/{teamId}`
5. Before expiry, sponsor renews: `PUT /sponsorships/{id}/renew?months=6`

### Shop Flow
1. Customer browses: `GET /products`
2. Customer searches: `GET /products/search?name=jersey&sportType=FOOTBALL`
3. Customer adds to cart: `POST /cart/add/{productId}?quantity=2`
4. Customer views cart: `GET /cart`
5. Customer checks out: `POST /orders/checkout`
6. Customer views orders: `GET /orders/my-orders`
7. Customer reviews product: `POST /products/{id}/reviews`

### Admin Flow
1. View sponsorship stats: `GET /sponsorships/admin/stats`
2. Check low stock: `GET /products/low-stock?threshold=10`
3. Approve pending sponsorships: `PUT /sponsorships/admin/{id}/approve`

---

## 📝 Next Steps (Future Features)

### Sponsorship Module
- GET /sponsorships/suggestions - AI recommendations for teams/events needing sponsors
- Notification system for expiring sponsorships
- Sponsorship analytics dashboard

### Shop Module
- Payment gateway integration (Tunisian payment methods)
- Order tracking system
- Inventory management automation
- Product recommendations based on user preferences
- Wishlist functionality
- Discount codes and promotions

---

## 🔧 Technical Notes

1. **Payment Proof**: Currently stores URL/path as String. For production, integrate with file upload service (AWS S3, local storage, etc.)

2. **Order Status**: Implement status transition logic (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED)

3. **Stock Management**: Consider adding automatic stock reduction on order placement

4. **Reviews**: Add validation to ensure user purchased the product before reviewing

5. **Tunisia Context**: Payment proof feature addresses local trust requirements for sponsorship transactions

---

## ✅ Implementation Checklist

### Sponsorship Module
- [x] GET /sponsorships/{id}
- [x] GET /sponsorships/team/{teamId}
- [x] GET /sponsorships/event/{eventId}
- [x] GET /sponsorships/venue/{venueId}
- [x] PUT /sponsorships/{id}/renew
- [x] POST /sponsorships/{id}/payment-proof
- [x] GET /sponsorships/admin/stats
- [x] Add paymentProof field to Sponsorship entity
- [x] Update SecurityConfig

### Shop Module
- [x] Order entity and repository
- [x] OrderItem entity and repository
- [x] ProductReview entity and repository
- [x] POST /orders/checkout
- [x] GET /orders/my-orders
- [x] GET /orders/{orderId}
- [x] POST /products/{id}/reviews
- [x] GET /products/{id}/reviews
- [x] GET /products/low-stock
- [x] Update User entity with orders relationship
- [x] Update Product entity with reviews relationship
- [x] Update SecurityConfig

---

## 🎉 Summary

All **must-have** endpoints have been implemented for both modules, plus several **nice-to-have** features. The implementation follows Spring Boot best practices with proper:
- JWT authentication and role-based authorization
- RESTful API design
- Entity relationships and JPA repositories
- Error handling and validation
- Security configuration

The platform now has a complete e-commerce flow (browse → cart → checkout → orders) and comprehensive sponsorship management with Tunisia-specific payment proof functionality.