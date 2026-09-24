package com.example.demoecom.service;

import com.example.demoecom.entity.Cart;
import com.example.demoecom.entity.CartItem;
import com.example.demoecom.entity.Product;
import com.example.demoecom.entity.User;
import com.example.demoecom.repository.CartItemRepository;
import com.example.demoecom.repository.CartRepository;
import com.example.demoecom.repository.ProductRepository;
import com.example.demoecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Cart getOrCreateCart() {
        String email = getCurrentUserEmail();

        return cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    public Cart addItemToCart(Long productId, Integer quantity) {
        Cart cart = getOrCreateCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // check if item already in cart -> increase quantity instead of duplicate row
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Cart cart = getOrCreateCart();

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(Long cartItemId) {
        Cart cart = getOrCreateCart();

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        if (!removed) {
            throw new IllegalArgumentException("Cart item not found");
        }

        return cartRepository.save(cart);
    }

    public Cart clearCart() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    public Cart getCart() {
        return getOrCreateCart();
    }
}