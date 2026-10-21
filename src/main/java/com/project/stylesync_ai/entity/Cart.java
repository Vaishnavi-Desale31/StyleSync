package com.project.stylesync_ai.entity;

import jakarta.persistence.*;
import com.project.stylesync_ai.entity.User;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
