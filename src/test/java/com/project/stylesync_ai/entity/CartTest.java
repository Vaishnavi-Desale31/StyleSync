package com.project.stylesync_ai.entity;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartTest {

    @Test
    void getTotalPrice_shouldCalculateTotalForMultipleItems(){

        Product tshirt = new Product();
        tshirt.setPrice(new BigDecimal("799.00"));

        CartItem item1 = new CartItem();
        item1.setProduct(tshirt);
        item1.setQuantity(2);

        Product jeans = new Product();
        jeans.setPrice(new BigDecimal("1299.00"));

        CartItem item2 = new CartItem();
        item2.setProduct(jeans);
        item2.setQuantity(1);

        Cart cart = new Cart();
        cart.addItem(item1);
        cart.addItem(item2);

        assertEquals(0, new BigDecimal("2897.00").compareTo(cart.getTotalPrice()));
    }

    @Test
    void getTotalPrice_shouldReturnZeroForEmptyCart(){

        Cart cart = new Cart();

        assertEquals(0, new BigDecimal("0.00").compareTo(cart.getTotalPrice()));
    }

    @Test
    void getTotalPrice_shouldReturnProductPriceForQuantityOne(){

        Product product = new Product();
        product.setPrice(new BigDecimal("799.00"));

        CartItem item1 = new CartItem();
        item1.setProduct(product);
        item1.setQuantity(1);

        Cart cart = new Cart();
        cart.addItem(item1);

        assertEquals(0, new BigDecimal("799.00").compareTo(cart.getTotalPrice()));

    }

    @Test
    void clearItems_shouldRemoveAllItemsAndDisconnectCart(){
        //Arrange :create a cart and two cart items
        Cart cart = new Cart();

        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();

        cart.addItem(item1);
        cart.addItem(item2);

        //Act: clear all items
        cart.clearItems();

        //Assert: cart is empty and items are disconnected
        assertTrue(cart.getItems().isEmpty());
        assertNull(item1.getCart());
        assertNull(item2.getCart());
    }

    @Test
    void getTotalPrice_shouldUpdateWhenQuantityChanges(){
        //Arrange: add one product with quantity 1
        Product product = new Product();
        product.setPrice(new BigDecimal("799.00"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        Cart cart = new Cart();
        cart.addItem(item);

        assertEquals(0, new BigDecimal("799.00").compareTo(cart.getTotalPrice()));

        //Act: increase the quantity to 3
        item.setQuantity(3);

        //Asset: total price should become 2397
        assertEquals(0, new BigDecimal("2397.00").compareTo(cart.getTotalPrice()));
    }
}