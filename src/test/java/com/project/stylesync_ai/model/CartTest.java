package com.project.stylesync_ai.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartTest {

    @Test
    void getTotalPrice_shouldCalculateTotalForMultipleItems(){

        Product tshirt = new Product();
        tshirt.setPrice(799.0);

        CartItem item1 = new CartItem();
        item1.setProduct(tshirt);
        item1.setQuantity(2);

        Product jeans = new Product();
        jeans.setPrice(1299.0);

        CartItem item2 = new CartItem();
        item2.setProduct(jeans);
        item2.setQuantity(1);

        Cart cart = new Cart();
        cart.addItem(item1);
        cart.addItem(item2);

        assertEquals(2897.0,cart.getTotalPrice(),0.01);
    }

    @Test
    void getTotalPrice_shouldReturnZeroForEmptyCart(){

        Cart cart = new Cart();

        assertEquals(0.0,cart.getTotalPrice(),0.001);
    }

    @Test
    void getTotalPrice_shouldReturnProductPriceForQuantityOne(){

        Product product = new Product();
        product.setPrice(799.0);

        CartItem item1 = new CartItem();
        item1.setProduct(product);
        item1.setQuantity(1);

        Cart cart = new Cart();
        cart.addItem(item1);

        assertEquals(799.0,cart.getTotalPrice(),0.001);

    }
}
