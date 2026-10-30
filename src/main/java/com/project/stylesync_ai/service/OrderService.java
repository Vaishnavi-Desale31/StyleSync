package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.entity.OrderItem;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService {
    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository){
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order createOrderFromCart(Long cartId, String email) {
        return createOrderFromCartInternal(cartId, email, null, null);
    }

    @Transactional
    public Order createCodOrderFromCart(Long cartId, String email) {
        return createOrderFromCartInternal(
                cartId,
                email,
                "COD",
                "PENDING"
        );
    }

    private Order createOrderFromCartInternal(
            Long cartId,
            String email,
            String paymentMethod,
            String paymentStatus) {

        Cart cart = cartRepository.findByIdAndUser_Email(cartId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot create an order from an empty cart"
            );
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.recordStatus("PLACED", "Order placed");

        if (paymentMethod != null) {
            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(paymentStatus);
        }

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();

            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setOrder(order);

            totalAmount += orderItem.getPrice()
                    * cartItem.getQuantity();

            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cart.clearItems();
        cartRepository.save(cart);

        return savedOrder;
    }

    /**
     * Buy Now: creates a Cash on Delivery order for a single product.
     * The price always comes from the database, never from the client,
     * and the customer's cart is neither read nor modified.
     */
    @Transactional
    public Order createCodOrderForProduct(
            Long productId,
            Integer quantity,
            String email) {

        if (productId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID is required"
            );
        }

        if (quantity == null || quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (product.getPrice() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This product cannot be purchased right now"
            );
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.recordStatus("PLACED", "Order placed");
        order.setPaymentMethod("COD");
        order.setPaymentStatus("PENDING");

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        orderItem.setOrder(order);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        order.setItems(orderItems);
        order.setTotalAmount(product.getPrice() * quantity);

        return orderRepository.save(order);
    }

    // ---------- Cancellation, delivery status and tracking ----------

    private static final Set<String> CANCELLABLE_STATUSES =
            Set.of("PAYMENT_PENDING", "PLACED", "CONFIRMED");

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS =
            Map.of(
                    "PLACED", Set.of("CONFIRMED"),
                    "CONFIRMED", Set.of("SHIPPED"),
                    "SHIPPED", Set.of("OUT_FOR_DELIVERY", "DELIVERED"),
                    "OUT_FOR_DELIVERY", Set.of("DELIVERED")
            );

    /** Customer cancellation: only the owner of the order can cancel it. */
    @Transactional
    public Order cancelOrder(Long orderId, String email, String reason) {
        Order order = orderRepository.findByIdAndUser_Email(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        return cancel(order, reason);
    }

    private Order cancel(Order order, String reason) {
        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order cannot be cancelled when it is "
                            + order.getStatus());
        }

        String cleanReason = (reason == null || reason.isBlank())
                ? "Cancelled" : reason.trim();

        if (cleanReason.length() > 255) {
            cleanReason = cleanReason.substring(0, 255);
        }

        // A paid online order needs a refund; the refund itself is
        // processed from the Razorpay dashboard.
        if ("PAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("REFUND_PENDING");
        } else {
            order.setPaymentStatus("CANCELLED");
        }

        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(cleanReason);
        order.recordStatus("CANCELLED", cleanReason);

        return orderRepository.save(order);
    }

    /** Admin: all orders, newest first. */
    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForAdmin() {
        List<Order> orders = new ArrayList<>(orderRepository.findAll());
        orders.sort((a, b) -> b.getId().compareTo(a.getId()));
        return orders;
    }

    /** Admin: move an order through the delivery lifecycle. */
    @Transactional
    public Order updateOrderStatus(
            Long orderId, String newStatus, String note) {

        if (newStatus == null || newStatus.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        String target = newStatus.trim().toUpperCase();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if ("CANCELLED".equals(target)) {
            return cancel(order, note);
        }

        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                order.getStatus(), Set.of());

        if (!allowed.contains(target)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot change order from " + order.getStatus()
                            + " to " + target);
        }

        String cleanNote = (note == null || note.isBlank())
                ? null : note.trim();

        if (cleanNote != null && cleanNote.length() > 255) {
            cleanNote = cleanNote.substring(0, 255);
        }

        if ("DELIVERED".equals(target)) {
            order.setDeliveredAt(LocalDateTime.now());

            if ("COD".equals(order.getPaymentMethod())) {
                order.setPaymentStatus("PAID");
            }
        }

        order.recordStatus(target, cleanNote);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, String email){
        return orderRepository.findByIdAndUser_Email(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders(String email) {
        return orderRepository.findAllByUser_Email(email);
    }
}