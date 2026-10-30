package com.atelier.repo;

import com.atelier.domain.CustomerOrder;
import com.atelier.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "customer")
    @Query("SELECT o FROM CustomerOrder o")
    Page<CustomerOrder> searchAll(Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    @Query("SELECT o FROM CustomerOrder o WHERE o.status = :status")
    Page<CustomerOrder> searchByStatus(@Param("status") OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    @Query("SELECT o FROM CustomerOrder o WHERE o.customer.id = :customerId")
    Page<CustomerOrder> searchByCustomer(@Param("customerId") Long customerId, Pageable pageable);
}
