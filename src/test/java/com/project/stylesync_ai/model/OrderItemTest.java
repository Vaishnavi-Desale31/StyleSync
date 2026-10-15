package com.project.stylesync_ai.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class OrderItemTest {
    @Test
    void shouldSetAndGetOrderItemsFields(){
        OrderItem item = new OrderItem();

        item.setProductId(2L);
        item.setProductName("Premium Black T-Shirt");
        item.setQuantity(2);
        item.setPrice(799.0);

        assertEquals(2L,item.getProductId());
        assertEquals("Premium Black T-Shirt", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(799.0, item.getPrice(),0.001);
    }

    @Test
    void shouldSetAndGetOrderRelationship(){
        Order order = new Order();
        OrderItem item = new OrderItem();

        item.setOrder(order);

        assertEquals(order,item.getOrder());

    }

    @Test
    void newOrderItemShouldHaveBeforePersistence(){
        OrderItem item = new OrderItem();

        assertNull(item.getId());
    }
}
