package tn.esprit.pi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private User user;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        user = new User();           // You may need to create a minimal User object
        user.setId(1L);              // Adjust according to your User class

        orderItem1 = new OrderItem();
        orderItem2 = new OrderItem();

        order = new Order();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(order);
        assertNull(order.getId());
        assertNull(order.getOrderDate());
        assertNull(order.getTotalAmount());
        assertNull(order.getStatus());
        assertNull(order.getShippingAddress());
        assertNull(order.getPhoneNumber());
        assertNull(order.getUser());
        assertNull(order.getOrderItems());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Set<OrderItem> items = new HashSet<>();
        items.add(orderItem1);

        Order fullOrder = new Order(
                10L,
                now,
                150.75,
                OrderStatus.PENDING,
                "123 Main Street, Tunis",
                "+216 98 765 432",
                user,
                items
        );

        assertEquals(10L, fullOrder.getId());
        assertEquals(now, fullOrder.getOrderDate());
        assertEquals(150.75, fullOrder.getTotalAmount());
        assertEquals(OrderStatus.PENDING, fullOrder.getStatus());
        assertEquals("123 Main Street, Tunis", fullOrder.getShippingAddress());
        assertEquals("+216 98 765 432", fullOrder.getPhoneNumber());
        assertEquals(user, fullOrder.getUser());
        assertEquals(items, fullOrder.getOrderItems());
    }

    @Test
    void testGettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();
        Set<OrderItem> items = new HashSet<>();
        items.add(orderItem1);
        items.add(orderItem2);

        order.setId(5L);
        order.setOrderDate(now);
        order.setTotalAmount(299.99);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setShippingAddress("Avenue Habib Bourguiba, Tunis");
        order.setPhoneNumber("+216 71 234 567");
        order.setUser(user);
        order.setOrderItems(items);

        assertEquals(5L, order.getId());
        assertEquals(now, order.getOrderDate());
        assertEquals(299.99, order.getTotalAmount());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals("Avenue Habib Bourguiba, Tunis", order.getShippingAddress());
        assertEquals("+216 71 234 567", order.getPhoneNumber());
        assertEquals(user, order.getUser());
        assertEquals(2, order.getOrderItems().size());
    }

    @Test
    void testOrderItemsRelationship() {
        Set<OrderItem> items = new HashSet<>();
        items.add(orderItem1);
        items.add(orderItem2);

        order.setOrderItems(items);

        assertNotNull(order.getOrderItems());
        assertEquals(2, order.getOrderItems().size());
        assertTrue(order.getOrderItems().contains(orderItem1));
    }

    @Test
    void testEqualsAndHashCode() {
        Order order1 = new Order();
        order1.setId(100L);

        Order order2 = new Order();
        order2.setId(100L);

        Order order3 = new Order();
        order3.setId(200L);

        assertEquals(order1, order2);
        assertNotEquals(order1, order3);
        assertEquals(order1.hashCode(), order2.hashCode());
    }
}