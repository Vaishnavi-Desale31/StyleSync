package com.project.stylesync_ai.entity;
import java.math.BigDecimal;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "orders")
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private LocalDateTime orderDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    private String status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "cart_id")
    private Long cartId;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    private LocalDateTime deliveredAt;

    // ---- Customer and delivery address SNAPSHOT --------------------
    // Copied from the user and the selected address when the order is
    // created. These are plain columns, not a foreign key, so editing
    // or deleting a saved address never changes an existing order.

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_email", length = 254)
    private String customerEmail;

    @Column(name = "ship_full_name", length = 100)
    private String shipFullName;

    @Column(name = "ship_phone", length = 20)
    private String shipPhone;

    @Column(name = "ship_line1", length = 200)
    private String shipLine1;

    @Column(name = "ship_line2", length = 200)
    private String shipLine2;

    @Column(name = "ship_city", length = 100)
    private String shipCity;

    @Column(name = "ship_state", length = 100)
    private String shipState;

    @Column(name = "ship_postal_code", length = 12)
    private String shipPostalCode;

    @Column(name = "ship_country", length = 100)
    private String shipCountry;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("createdAt ASC, id ASC")
    private List<OrderStatusEvent> statusHistory = new ArrayList<>();

    public Order() {
    }

    /**
     * Changes the order status and appends an entry to the tracking
     * timeline.
     */
    public void recordStatus(String newStatus, String note) {
        this.status = newStatus;
        this.statusHistory.add(new OrderStatusEvent(this, newStatus, note));
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public List<OrderStatusEvent> getStatusHistory() {
        return statusHistory;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    /**
     * Copies the customer's details and the chosen delivery address
     * onto this order. Call after setUser(...).
     */
    public void applyDeliveryAddress(Address address) {
        if (user != null) {
            this.customerName = user.getName();
            this.customerEmail = user.getEmail();
        }

        this.shipFullName = address.getFullName();
        this.shipPhone = address.getPhone();
        this.shipLine1 = address.getLine1();
        this.shipLine2 = address.getLine2();
        this.shipCity = address.getCity();
        this.shipState = address.getState();
        this.shipPostalCode = address.getPostalCode();
        this.shipCountry = address.getCountry();
    }

    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShipFullName() { return shipFullName; }
    public String getShipPhone() { return shipPhone; }
    public String getShipLine1() { return shipLine1; }
    public String getShipLine2() { return shipLine2; }
    public String getShipCity() { return shipCity; }
    public String getShipState() { return shipState; }
    public String getShipPostalCode() { return shipPostalCode; }
    public String getShipCountry() { return shipCountry; }
}