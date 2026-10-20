
package com.project.stylesync_ai.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void shouldSetAndGetOrderFields() {
        Order order = new Order();
        LocalDateTime now = LocalDateTime.now();

        order.setOrderDate(now);
        order.setStatus("PLACED");
        order.setTotalAmount(1598.0);

        assertEquals(now, order.getOrderDate());
        assertEquals("PLACED", order.getStatus());
        assertEquals(1598.0, order.getTotalAmount(), 0.001);
    }

    @Test
    void newOrderShouldHaveAnEmptyItemsList() {
        Order order = new Order();

        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void shouldSetAndGetOrderItems() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        ArrayList<OrderItem> items = new ArrayList<>();

        items.add(item);
        order.setItems(items);

        assertEquals(1, order.getItems().size());
        assertSame(item, order.getItems().get(0));
    }
}
