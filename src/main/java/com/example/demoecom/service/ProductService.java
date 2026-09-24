package com.example.demoecom.service;

import com.example.demoecom.dto.ProductDTO;
import com.example.demoecom.entity.Product;
import com.example.demoecom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Cached under key "all" in the "products" cache
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Each product cached individually by its id
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    // Category results cached by category name
    @Cacheable(value = "products", key = "'category_' + #category")
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // Search results are not cached — keyword queries are too varied to be worth caching
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    // Evict both caches on any write operation to keep data consistent
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product",  allEntries = true)
    })
    public Product createProduct(ProductDTO dto) {
        Product product = new Product();
        mapDtoToEntity(dto, product);
        return productRepository.save(product);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product",  key = "#id")
    })
    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = getProductById(id);
        mapDtoToEntity(dto, product);
        return productRepository.save(product);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product",  key = "#id")
    })
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    private void mapDtoToEntity(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(dto.getCategory());
        product.setStockQty(dto.getStockQty());
    }
}
