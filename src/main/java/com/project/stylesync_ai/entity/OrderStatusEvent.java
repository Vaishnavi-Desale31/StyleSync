package com.project.stylesync_ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * One entry in an order's tracking timeline.
 */
@Entity
@Table(name = "order_status_events")
public class OrderStatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 255)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public OrderStatusEvent() {
    }

    public OrderStatusEvent(Order order, String status, String note) {
        this.order = order;
        this.status = status;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}