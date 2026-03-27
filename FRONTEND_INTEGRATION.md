# Frontend Integration Guide

## Base URL
```
http://localhost:8089/SpringSecurity
```

## Authentication
All authenticated requests need JWT token in header:
```typescript
headers: { 'Authorization': `Bearer ${token}` }
```

---

## 🎯 NEW Sponsorship Endpoints

### Public (No Auth)
```typescript
// Get sponsorship details
GET /sponsorships/{id}

// Get sponsorships by context
GET /sponsorships/team/{teamId}
GET /sponsorships/event/{eventId}
GET /sponsorships/venue/{venueId}
GET /sponsorships/pending
GET /sponsorships/active
```

### Sponsor Role (Auth Required)
```typescript
// Submit sponsorship
POST /sponsorships/submit
Body: {
  amount: number,
  startDate: "YYYY-MM-DD",
  endDate: "YYYY-MM-DD",
  team?: { id: number },
  event?: { id: number },
  venue?: { id: number }
}

// Get my sponsorships
GET /sponsorships/my-sponsorships

// Renew sponsorship
PUT /sponsorships/{id}/renew?months=6

// Upload payment proof (IMPORTANT for Tunisia)
POST /sponsorships/{id}/payment-proof?proofUrl=string

// Cancel sponsorship
DELETE /sponsorships/{id}/cancel
```

### Admin Role (Auth Required)
```typescript
// Get statistics
GET /sponsorships/admin/stats
Response: {
  totalActive: number,
  totalAmountThisMonth: number,
  totalPending: number
}

// Approve/Reject
PUT /sponsorships/admin/{id}/approve
PUT /sponsorships/admin/{id}/reject

// Update/Delete
PUT /sponsorships/admin/{id}
DELETE /sponsorships/admin/{id}
```

---

## 🛒 NEW Shop & Order Endpoints

### Products (Public)
```typescript
// Already exists - no changes
GET /products
GET /products/{id}
GET /products/search?name=&category=&sportType=
GET /products/price-range?minPrice=&maxPrice=
```

### Reviews
```typescript
// Get reviews (Public)
GET /products/{id}/reviews
Response: ProductReview[]

// Add review (Auth Required)
POST /products/{id}/reviews
Body: {
  rating: number, // 1-5
  comment: string
}
```

### Cart (Auth Required)
```typescript
// Already exists - no changes
GET /cart
POST /cart/add/{productId}?quantity=number
PUT /cart/update/{itemId}?quantity=number
DELETE /cart/remove/{itemId}
DELETE /cart/clear
```

### Orders (Auth Required)
```typescript
// Checkout - creates order from cart
POST /orders/checkout
Body: {
  shippingAddress: string,
  phoneNumber: string
}
Response: {
  message: string,
  orderId: number
}

// Get my orders
GET /orders/my-orders
Response: Order[] // sorted by date desc

// Get order details
GET /orders/{orderId}
Response: Order
```

### Admin (Auth Required)
```typescript
// Low stock alert
GET /products/low-stock?threshold=5
Response: Product[]
```

---

## 📦 TypeScript Interfaces

```typescript
// Sponsorship
interface Sponsorship {
  id: number;
  amount: number;
  startDate: string; // "YYYY-MM-DD"
  endDate: string;
  paymentProof?: string; // NEW
  status: 'PENDING' | 'ACTIVE' | 'REJECTED';
  sponsorProfile: SponsorProfile;
  team?: Team;
  event?: Event;
  venue?: Venue;
}

// Order (NEW)
interface Order {
  id: number;
  orderDate: string; // ISO DateTime
  totalAmount: number;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  shippingAddress: string;
  phoneNumber: string;
  orderItems: OrderItem[];
}

// OrderItem (NEW)
interface OrderItem {
  id: number;
  quantity: number;
  price: number; // price at order time
  subtotal: number;
  product: Product;
}

// ProductReview (NEW)
interface ProductReview {
  id: number;
  rating: number; // 1-5
  comment: string;
  createdAt: string; // ISO DateTime
  user: User;
}

// Cart (existing)
interface Cart {
  id: number;
  totalAmount: number;
  cartItems: CartItem[];
}

// Product (existing)
interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  category: string;
  image: string;
  sportType: SportType;
}
```

---

## 🔐 Role-Based Access

```typescript
// Route guards needed for:
SPONSOR: [
  '/sponsorships/submit',
  '/sponsorships/my-sponsorships',
  '/sponsorships/:id/renew',
  '/sponsorships/:id/payment-proof',
  '/sponsorships/:id/cancel'
]

ADMIN: [
  '/sponsorships/admin/**',
  '/products/low-stock'
]

AUTHENTICATED: [
  '/cart/**',
  '/orders/**',
  '/products/:id/reviews' (POST only)
]
```

---

## 🎨 UI Components Needed

### Sponsorship Module
1. **Sponsorship List** - Display by team/event/venue
2. **Sponsorship Detail** - Show payment proof
3. **Sponsor Dashboard** - My sponsorships + renew button
4. **Payment Proof Upload** - File upload component
5. **Admin Stats Dashboard** - Statistics display
6. **Admin Approval Panel** - Approve/reject pending

### Shop Module
1. **Product Reviews Section** - Display + add review
2. **Checkout Form** - Shipping address + phone
3. **Order History** - List past orders
4. **Order Detail** - Show items + status
5. **Order Status Badge** - Visual status indicator
6. **Low Stock Alert** (Admin) - Product list

---

## 🚨 Important Notes

1. **Payment Proof**: Store file URL after upload to your file service, then send URL to backend
2. **Order Status**: Display with color coding (PENDING=yellow, DELIVERED=green, etc.)
3. **Reviews**: Only show "Add Review" button for authenticated users
4. **Cart → Order**: Cart is automatically cleared after successful checkout
5. **Date Format**: Backend expects "YYYY-MM-DD" for dates
6. **Sponsorship Renewal**: Calculate new end date on frontend before sending months parameter

---

## 🧪 Testing Checklist

- [ ] Sponsor can submit sponsorship
- [ ] Sponsor can upload payment proof
- [ ] Public can view team/event/venue sponsorships
- [ ] User can add products to cart
- [ ] User can checkout and create order
- [ ] User can view order history
- [ ] User can add product review
- [ ] Admin can see sponsorship stats
- [ ] Admin can approve/reject sponsorships
- [ ] Admin can see low stock products

---

## 📞 Questions?

Refer to `API_REFERENCE.md` for detailed examples and `SPONSORSHIP_SHOP_ENHANCEMENTS.md` for complete documentation.
