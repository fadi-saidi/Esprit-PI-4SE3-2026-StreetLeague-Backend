# Street League - Amateur Sports Platform

A unified digital platform for amateur sports management with JWT authentication and role-based access control.

## Features

- **User Management**: Multiple user roles (Admin, Player, Coach, Referee, Health Professional, Sponsor, Venue Owner)
- **Event Management**: Matches, tournaments, and training sessions
- **Venue Booking**: Reserve sports fields and venues
- **Health Tracking**: Medical records and injury management
- **Digital Wallet**: Secure payment system
- **Community**: Posts, comments, and social features
- **Carpooling**: Coordinate transportation to events
- **Fantasy Sports**: Virtual teams and rewards
- **Shop & E-commerce**: Complete shopping experience with cart, checkout, orders, and reviews
- **Player Merchandise**: Players can submit merchandise for admin approval before selling
- **Sponsorship Management**: Submit, approve, and manage sponsorships with payment proof

## Tech Stack

- **Backend**: Spring Boot 4.0.3, Java 21
- **Security**: Spring Security + JWT
- **Database**: MySQL
- **ORM**: JPA/Hibernate

## API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

### Sponsorship Management
- `GET /sponsorships/{id}` - View sponsorship details (Public)
- `GET /sponsorships/team/{teamId}` - List team sponsorships (Public)
- `GET /sponsorships/event/{eventId}` - List event sponsorships (Public)
- `GET /sponsorships/venue/{venueId}` - List venue sponsorships (Public)
- `POST /sponsorships/submit` - Submit sponsorship request (Sponsor)
- `GET /sponsorships/my-sponsorships` - View my sponsorships (Sponsor)
- `PUT /sponsorships/{id}/renew` - Renew sponsorship (Sponsor)
- `POST /sponsorships/{id}/payment-proof` - Upload payment proof (Sponsor)
- `GET /sponsorships/admin/stats` - Admin statistics (Admin)
- `PUT /sponsorships/admin/{id}/approve` - Approve sponsorship (Admin)
- `PUT /sponsorships/admin/{id}/reject` - Reject sponsorship (Admin)

### Player Merchandise
- `POST /player-merch/submit` - Submit merchandise for approval (Player)
- `GET /player-merch/my-submissions` - View my submissions (Player)
- `PUT /player-merch/{id}` - Update pending merchandise (Player)
- `GET /player-merch/admin/pending` - View pending submissions (Admin)
- `PUT /player-merch/admin/{id}/approve` - Approve merchandise (Admin)
- `PUT /player-merch/admin/{id}/reject` - Reject merchandise (Admin)
- `GET /player-merch/admin/stats` - Admin statistics (Admin)

### Wallet Integration
- `GET /wallet/balance` - Get wallet balance and points (Authenticated)
- `GET /wallet/transactions` - Get transaction history (Authenticated)
- `GET /wallet/stats` - Get wallet statistics (Authenticated)

### Shop & Products
- `GET /products` - List all products (Public)
- `GET /products/{id}` - Product details (Public)
- `GET /products/search` - Search & filter products (Public)
- `GET /products/{id}/reviews` - View product reviews (Public)
- `POST /products/{id}/reviews` - Add product review (Authenticated)
- `GET /products/low-stock` - Low stock alert (Admin)

### Shopping Cart
- `GET /cart` - View cart (Authenticated)
- `POST /cart/add/{productId}` - Add to cart (Authenticated)
- `PUT /cart/update/{itemId}` - Update cart item (Authenticated)
- `DELETE /cart/remove/{itemId}` - Remove from cart (Authenticated)

### Orders
- `POST /orders/checkout` - Create order from cart (Authenticated)
- `GET /orders/my-orders` - View my orders (Authenticated)
- `GET /orders/{orderId}` - Order details (Authenticated)

### Role-Based Access
- `/admin/**` - Admin only
- `/player/**` - Player only
- `/coach/**` - Coach only
- `/referee/**` - Referee only
- `/health/**` - Health Professional only
- `/sponsor/**` - Sponsor only
- `/venue/**` - Venue Owner only

## Setup

1. Clone the repository
2. Configure MySQL database in `application.properties`
3. Run database migration: `mysql -u root -p pi < database_migration.sql`
4. Run: `mvn spring-boot:run`
5. Server runs on: `http://localhost:8089/SpringSecurity`

## Documentation

- **API Reference**: See `API_REFERENCE.md` for detailed endpoint documentation
- **Module Enhancements**: See `SPONSORSHIP_SHOP_ENHANCEMENTS.md` for feature details
- **Database Migration**: See `database_migration.sql` for schema updates

## Database Configuration

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pi
spring.datasource.username=root
spring.datasource.password=
```

## User Roles

- `ADMIN` - Platform administration
- `PLAYER` - Athletes and players
- `COACH` - Team coaches
- `REFEREE` - Match officials
- `HEALTH_PROFESSIONAL` - Medical staff
- `SPONSOR` - Event sponsors
- `VENUE_OWNER` - Field/venue owners

## Security

- JWT-based stateless authentication
- Role-based authorization
- CORS enabled for Angular frontend (localhost:4200)
- Password encryption with BCrypt
