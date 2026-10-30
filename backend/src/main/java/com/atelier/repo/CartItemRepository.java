package com.atelier.repo;

import com.atelier.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    boolean existsByProductId(Long productId);
}
