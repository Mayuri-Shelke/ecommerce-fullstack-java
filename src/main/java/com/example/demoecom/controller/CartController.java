package com.example.demoecom.controller;

import com.example.demoecom.entity.Cart;
import com.example.demoecom.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addItem(@RequestParam Long productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(productId, quantity));
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable Long cartItemId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartItemId, quantity));
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Cart> removeItem(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartItemId));
    }

    @DeleteMapping
    public ResponseEntity<Cart> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}