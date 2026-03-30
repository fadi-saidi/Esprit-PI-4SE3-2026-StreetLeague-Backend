# Player Merchandise System Documentation

## Overview

The Player Merchandise System allows players to submit their own merchandise for sale on the platform, subject to admin approval. This creates a marketplace where players can monetize their brand while maintaining quality control through an approval workflow.

## Features

### For Players
- Submit merchandise with details (name, description, price, stock, category, image, sport type)
- View all their submissions with status tracking
- Update pending merchandise before approval
- Track approval/rejection status with timestamps

### For Admins
- Review pending merchandise submissions
- Approve merchandise (automatically creates products in shop)
- Reject merchandise with detailed reasons
- View statistics on submissions
- Maintain quality control over marketplace

## Database Schema

### PlayerMerch Entity
```sql
CREATE TABLE player_merch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    category VARCHAR(100) NOT NULL,
    image VARCHAR(500),
    sport_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    seller_id BIGINT NOT NULL,
    approved_by BIGINT,
    submitted_at DATETIME NOT NULL,
    approved_at DATETIME,
    rejection_reason TEXT,
    product_id BIGINT,
    -- Foreign Keys --
    CONSTRAINT fk_player_merch_seller FOREIGN KEY (seller_id) REFERENCES player_profile(id),
    CONSTRAINT fk_player_merch_approver FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_player_merch_product FOREIGN KEY (product_id) REFERENCES product(id)
);
```

### Status Flow
```
PENDING → APPROVED (creates Product)
PENDING → REJECTED (with reason)
```

## API Endpoints

### Player Endpoints

#### Submit Merchandise
```http
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
```

**Response:**
```json
{
  "id": 1,
  "name": "Custom Team Jersey",
  "description": "High-quality jersey with team logo",
  "price": 45.99,
  "stock": 20,
  "category": "apparel",
  "image": "https://example.com/jersey.jpg",
  "sportType": "FOOTBALL",
  "status": "PENDING",
  "sellerName": "john_player",
  "sellerId": 123,
  "submittedAt": "2024-01-15T10:30:00",
  "approvedAt": null,
  "rejectionReason": null
}
```

#### Get My Submissions
```http
GET /player-merch/my-submissions
Authorization: Bearer {jwt_token}
```

#### Update Pending Merchandise
```http
PUT /player-merch/{id}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "Updated Jersey Name",
  "description": "Updated description",
  "price": 49.99,
  "stock": 25,
  "category": "apparel",
  "image": "https://example.com/jersey-updated.jpg",
  "sportType": "FOOTBALL"
}
```

### Admin Endpoints

#### Get Pending Submissions
```http
GET /player-merch/admin/pending?page=0&pageSize=20
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Custom Team Jersey",
      "description": "High-quality jersey with team logo",
      "price": 45.99,
      "stock": 20,
      "category": "apparel",
      "image": "https://example.com/jersey.jpg",
      "sportType": "FOOTBALL",
      "status": "PENDING",
      "sellerName": "john_player",
      "sellerId": 123,
      "submittedAt": "2024-01-15T10:30:00",
      "approvedAt": null,
      "rejectionReason": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

#### Approve Merchandise
```http
PUT /player-merch/admin/{id}/approve
Authorization: Bearer {jwt_token}
```

**What happens:**
1. Status changes to `APPROVED`
2. `approved_by` set to admin user
3. `approved_at` set to current timestamp
4. New `Product` created in shop
5. Product linked to "Player Marketplace" shop

#### Reject Merchandise
```http
PUT /player-merch/admin/{id}/reject
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "reason": "Image quality is too low. Please provide higher resolution images."
}
```

#### Get Statistics
```http
GET /player-merch/admin/stats
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "pending": 5,
  "approved": 23,
  "rejected": 2
}
```

## Business Logic

### Submission Rules
- Only users with `PLAYER` role can submit merchandise
- Player must have a valid `PlayerProfile`
- All required fields must be provided
- Price must be positive
- Stock cannot be negative

### Update Rules
- Only the seller can update their own merchandise
- Can only update merchandise with `PENDING` status
- Cannot update after approval/rejection

### Approval Process
1. Admin reviews pending submissions
2. On approval:
   - Creates new `Product` in database
   - Links to "Player Marketplace" shop (auto-created if needed)
   - Sets merchandise status to `APPROVED`
   - Records approval timestamp and admin
3. On rejection:
   - Sets status to `REJECTED`
   - Records rejection reason
   - Records approval timestamp and admin

### Shop Integration
- Approved merchandise becomes regular products
- Appears in product listings and search
- Can be purchased through normal shopping flow
- Reviews and ratings work normally

## Validation Rules

### PlayerMerchRequest
- `name`: Required, 2-100 characters
- `description`: Optional, max 500 characters
- `price`: Required, must be positive
- `stock`: Required, cannot be negative
- `category`: Required
- `image`: Optional URL
- `sportType`: Required enum value

### MerchApprovalRequest (for rejection)
- `reason`: Required for rejection

## Error Handling

### Common Errors
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Wrong role or not owner of merchandise
- `404 Not Found`: Merchandise ID doesn't exist
- `400 Bad Request`: Invalid data or business rule violation

### Specific Error Messages
- "Only players can submit merchandise"
- "Player profile not found"
- "You can only update your own merchandise"
- "Can only update pending merchandise"
- "Can only approve pending merchandise"
- "Admin access required"

## Integration Points

### With Shop System
- Approved merchandise becomes `Product` entities
- Linked to "Player Marketplace" shop
- Inherits all shop functionality (cart, orders, reviews)

### With User System
- Requires `PLAYER` role for submission
- Requires `ADMIN` role for approval/rejection
- Links to `PlayerProfile` for seller information

### With Wallet System (Future)
When wallet system is ready:
- Track sales revenue for players
- Handle commission/fees
- Payment processing for merchandise sales

## Frontend Requirements

### Player Dashboard
- Merchandise submission form
- My submissions list with status indicators
- Edit pending submissions
- Status tracking (pending/approved/rejected)

### Admin Panel
- Pending submissions queue
- Approval/rejection interface
- Statistics dashboard
- Bulk actions for multiple submissions

### Shop Integration
- Filter products by "Player Merchandise"
- Show seller information on product pages
- Special badges for player-created products

## Testing Scenarios

### Happy Path
1. Player submits merchandise
2. Admin approves it
3. Product appears in shop
4. Customer can purchase it

### Edge Cases
- Player tries to update approved merchandise (should fail)
- Non-player tries to submit merchandise (should fail)
- Admin tries to approve already approved merchandise (should fail)
- Player tries to approve their own merchandise (should fail)

## Security Considerations

- Role-based access control enforced
- Players can only manage their own submissions
- Admins have full approval/rejection rights
- Input validation prevents malicious data
- File upload security for images (if implemented)

## Performance Considerations

- Pagination for admin pending list
- Indexes on status and seller_id for fast queries
- Efficient product creation on approval
- Consider caching for statistics

## Future Enhancements

1. **Revenue Sharing**: Track sales and distribute revenue to players
2. **Bulk Operations**: Admin bulk approve/reject
3. **Categories Management**: Dynamic category system
4. **Image Upload**: Direct image upload instead of URLs
5. **Notifications**: Email/push notifications for status changes
6. **Analytics**: Detailed sales analytics for players
7. **Inventory Management**: Auto-update stock levels
8. **Seasonal Promotions**: Special pricing for player merchandise