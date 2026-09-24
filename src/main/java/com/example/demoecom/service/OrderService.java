package com.example.demoecom.service;

import com.example.demoecom.entity.*;
import com.example.demoecom.entity.Order.OrderStatus;
import com.example.demoecom.repository.OrderRepository;
import com.example.demoecom.repository.ProductRepository;
import com.example.demoecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Converts the current user's cart into a placed order.
     * Validates stock, deducts quantities, snapshots prices, then clears the cart.
     */
    @Transactional
    public Order checkout() {
        String email = getCurrentUserEmail();

        Cart cart = cartService.getCart();

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart");
        }

        // Validate stock availability for all items before touching anything
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.getName()
                        + " (available: " + product.getStockQty()
                        + ", requested: " + cartItem.getQuantity() + ")"
                );
            }
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = new Order();
        order.setUser(user);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Deduct stock
            product.setStockQty(product.getStockQty() - cartItem.getQuantity());
            productRepository.save(product);

            // Build order item with price snapshot
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            order.getItems().add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // Clear the cart after successful order placement
        cartService.clearCart();

        return savedOrder;
    }

    /**
     * Returns all orders for the current user, newest first.
     */
    public List<Order> getOrderHistory() {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(getCurrentUserEmail());
    }

    /**
     * Returns a single order — scoped to the current user so users can't peek at each other's orders.
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findByIdAndUserEmail(orderId, getCurrentUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
    }

    /**
     * Admin-only: update the status of any order.
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
