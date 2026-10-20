package com.project.stylesync_ai.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    public Cart(){

    }

    public Long getId(){
        return id;
    }

    public List<CartItem> getItems(){
        return items;
    }

    public void setItems(List<CartItem> items){
        this.items = items;
    }

    public void addItem(CartItem item){
       items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item){
        items.remove(item);
        item.setCart(null);
    }

    public void clearItems(){
        for(CartItem item : new ArrayList<>(items)){
            removeItem(item);
        }
    }

    public Double getTotalPrice(){
        return items.stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice() * item.getQuantity()
                )
                .sum();
    }
}
