package com.example.demoecom.controller;

import com.example.demoecom.entity.Order;
import com.example.demoecom.entity.Order.OrderStatus;
import com.example.demoecom.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders/checkout
     * Converts the current user's cart into an order.
     * Requires: authenticated user with items in cart.
     */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout() {
        return ResponseEntity.ok(orderService.checkout());
    }

    /**
     * GET /api/orders
     * Returns order history for the currently authenticated user, newest first.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrderHistory() {
        return ResponseEntity.ok(orderService.getOrderHistory());
    }

    /**
     * GET /api/orders/{id}
     * Returns a single order scoped to the current user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * PUT /api/orders/{id}/status?status=CONFIRMED
     * Admin-only (restricted in SecurityConfig). Updates the order status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
