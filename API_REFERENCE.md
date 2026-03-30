# API Quick Reference Guide

## Base URL
```
http://localhost:8089/SpringSecurity
```

---

## 🎯 Sponsorship Endpoints

### Public Endpoints

```http
# Get sponsorship by ID
GET /sponsorships/{id}

# Get all sponsorships for a team
GET /sponsorships/team/{teamId}

# Get all sponsorships for an event
GET /sponsorships/event/{eventId}

# Get all sponsorships for a venue
GET /sponsorships/venue/{venueId}

# Get pending sponsorships
GET /sponsorships/pending

# Get active sponsorships
GET /sponsorships/active
```

### Sponsor Endpoints (Requires SPONSOR role)

```http
# Submit new sponsorship request
POST /sponsorships/submit
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "amount": 5000.0,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "team": { "id": 1 },
  "event": null,
  "venue": null
}

# Get my sponsorships
GET /sponsorships/my-sponsorships
Authorization: Bearer {jwt_token}

# Renew sponsorship
PUT /sponsorships/{id}/renew?months=6
Authorization: Bearer {jwt_token}

# Upload payment proof
POST /sponsorships/{id}/payment-proof?proofUrl=https://example.com/proof.pdf
Authorization: Bearer {jwt_token}

# Cancel sponsorship
DELETE /sponsorships/{id}/cancel
Authorization: Bearer {jwt_token}
```

### Admin Endpoints (Requires ADMIN role)

```http
# Get sponsorship statistics
GET /sponsorships/admin/stats
Authorization: Bearer {jwt_token}

# Approve sponsorship
PUT /sponsorships/admin/{id}/approve
Authorization: Bearer {jwt_token}

# Reject sponsorship
PUT /sponsorships/admin/{id}/reject
Authorization: Bearer {jwt_token}

# Update sponsorship
PUT /sponsorships/admin/{id}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "amount": 6000.0,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}

# Delete sponsorship
DELETE /sponsorships/admin/{id}
Authorization: Bearer {jwt_token}
```

---

## 🛒 Shop & Product Endpoints

### Public Product Endpoints

```http
# Get all products
GET /products

# Get product by ID
GET /products/{id}

# Search products
GET /products/search?name=jersey&category=apparel&sportType=FOOTBALL

# Filter by price range
GET /products/price-range?minPrice=10&maxPrice=100

# Get product reviews
GET /products/{id}/reviews
```

### Player Merchandise Endpoints

#### Player Endpoints (Requires PLAYER role)

```http
# Submit merchandise for approval
POST /player-merch/submit
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "Custom Team Jersey",
  "description": "High-quality jersey with team logo",
  "price": 45.99,
  "stock": 20,
  "category": "apparel",
  "image": "https://example.com/jersey.jpg",
  "sportType": "FOOTBALL"
}

# Get my merchandise submissions
GET /player-merch/my-submissions
Authorization: Bearer {jwt_token}

# Update pending merchandise
PUT /player-merch/{id}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "Updated Jersey",
  "description": "Updated description",
  "price": 49.99,
  "stock": 25,
  "category": "apparel",
  "image": "https://example.com/jersey-updated.jpg",
  "sportType": "FOOTBALL"
}
```

#### Admin Endpoints (Requires ADMIN role)

```http
# Get pending merchandise submissions
GET /player-merch/admin/pending?page=0&pageSize=20
Authorization: Bearer {jwt_token}

# Get merchandise statistics
GET /player-merch/admin/stats
Authorization: Bearer {jwt_token}

# Approve merchandise (creates product)
PUT /player-merch/admin/{id}/approve
Authorization: Bearer {jwt_token}

# Reject merchandise
PUT /player-merch/admin/{id}/reject
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "reason": "Image quality is too low. Please provide higher resolution images."
}
```

### Wallet Endpoints (Requires Authentication)

```http
# Get wallet balance
GET /wallet/balance
Authorization: Bearer {jwt_token}

# Get transaction history
GET /wallet/transactions
Authorization: Bearer {jwt_token}

# Get wallet statistics
GET /wallet/stats
Authorization: Bearer {jwt_token}
```

### Cart Endpoints (Requires Authentication)

```http
# Get my cart
GET /cart
Authorization: Bearer {jwt_token}

# Add product to cart
POST /cart/add/{productId}?quantity=2
Authorization: Bearer {jwt_token}

# Update cart item quantity
PUT /cart/update/{itemId}?quantity=3
Authorization: Bearer {jwt_token}

# Remove item from cart
DELETE /cart/remove/{itemId}
Authorization: Bearer {jwt_token}

# Clear entire cart
DELETE /cart/clear
Authorization: Bearer {jwt_token}
```

### Order Endpoints (Requires Authentication)

```http
# Checkout (create order from cart)
POST /orders/checkout
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "shippingAddress": "123 Main Street, Tunis, Tunisia",
  "phoneNumber": "+216 12 345 678"
}

# Get my orders
GET /orders/my-orders
Authorization: Bearer {jwt_token}

# Get order details
GET /orders/{orderId}
Authorization: Bearer {jwt_token}
```

### Review Endpoints (Requires Authentication)

```http
# Add product review
POST /products/{id}/reviews
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "rating": 5,
  "comment": "Excellent product! Fast delivery and great quality."
}
```

### Admin Product Endpoints (Requires ADMIN role)

```http
# Get low stock products
GET /products/low-stock?threshold=5
Authorization: Bearer {jwt_token}

# Create product
POST /products
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "Football Jersey",
  "price": 49.99,
  "stock": 100,
  "category": "apparel",
  "image": "https://example.com/jersey.jpg",
  "sportType": "FOOTBALL"
}

# Update product
PUT /products/{id}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "Updated Jersey",
  "price": 45.99,
  "stock": 80,
  "category": "apparel",
  "image": "https://example.com/jersey-new.jpg",
  "sportType": "FOOTBALL"
}

# Delete product
DELETE /products/{id}
Authorization: Bearer {jwt_token}
```

---

## 🔐 Authentication

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "phone": "+216 12 345 678",
  "role": "PLAYER",
  "address": "Tunis, Tunisia"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "role": "PLAYER"
}
```

---

## 📋 Enums Reference

### SponsorshipStatus
- `PENDING` - Awaiting admin approval
- `ACTIVE` - Approved and active
- `REJECTED` - Rejected by admin

### OrderStatus
- `PENDING` - Order placed, awaiting confirmation
- `CONFIRMED` - Order confirmed
- `PROCESSING` - Being prepared
- `SHIPPED` - Out for delivery
- `DELIVERED` - Successfully delivered
- `CANCELLED` - Order cancelled

### MerchStatus
- `PENDING` - Awaiting admin approval
- `APPROVED` - Approved and converted to product
- `REJECTED` - Rejected by admin

### SportType
- `FOOTBALL`
- `BASKETBALL`
- `TENNIS`
- `VOLLEYBALL`
- `HANDBALL`
- `RUGBY`

### Role
- `ADMIN`
- `PLAYER`
- `COACH`
- `REFEREE`
- `HEALTH_PROFESSIONAL`
- `SPONSOR`
- `VENUE_OWNER`

---

## 🧪 Testing Scenarios

### Scenario 1: Complete Sponsorship Flow
1. Register as SPONSOR
2. Login and get JWT token
3. Submit sponsorship: `POST /sponsorships/submit`
4. Admin approves: `PUT /sponsorships/admin/{id}/approve`
5. Upload payment proof: `POST /sponsorships/{id}/payment-proof`
6. View on team page: `GET /sponsorships/team/{teamId}`
7. Renew before expiry: `PUT /sponsorships/{id}/renew?months=6`

### Scenario 2: Complete Shopping Flow
1. Register as PLAYER
2. Login and get JWT token
3. Browse products: `GET /products`
4. Search: `GET /products/search?name=jersey`
5. Add to cart: `POST /cart/add/1?quantity=2`
6. View cart: `GET /cart`
7. Checkout: `POST /orders/checkout`
8. View orders: `GET /orders/my-orders`
9. Review product: `POST /products/1/reviews`

### Scenario 3: Player Merchandise Flow
1. Register as PLAYER
2. Login and get JWT token
3. Submit merchandise: `POST /player-merch/submit`
4. Check submission status: `GET /player-merch/my-submissions`
5. Admin reviews: `GET /player-merch/admin/pending`
6. Admin approves: `PUT /player-merch/admin/{id}/approve`
7. Merchandise becomes available as product in shop
8. Players can update pending submissions: `PUT /player-merch/{id}`

### Scenario 4: Admin Management
1. Login as ADMIN
2. View sponsorship stats: `GET /sponsorships/admin/stats`
3. Check low stock: `GET /products/low-stock`
4. Approve pending sponsorships: `GET /sponsorships/pending` then `PUT /sponsorships/admin/{id}/approve`

---

## 💡 Tips

1. **JWT Token**: Include in Authorization header as `Bearer {token}`
2. **CORS**: Frontend at `http://localhost:4200` is allowed
3. **Date Format**: Use ISO format `YYYY-MM-DD` for dates
4. **IDs**: All entity IDs are Long type
5. **Validation**: Ensure required fields are provided in request bodies

---

## 🐛 Common Issues

### 403 Forbidden
- Check if JWT token is valid and not expired
- Verify user has correct role for the endpoint

### 404 Not Found
- Verify entity ID exists in database
- Check endpoint URL is correct

### 400 Bad Request
- Validate request body JSON format
- Ensure all required fields are provided
- Check data types match entity fields

---

## 📞 Support

For issues or questions, refer to:
- Main README: `README.md`
- Detailed documentation: `SPONSORSHIP_SHOP_ENHANCEMENTS.md`
- Security config: `SecurityConfig.java`
