package com.restaurant.restaurantorderingsystem.repository;

import com.restaurant.restaurantorderingsystem.entity.Order;
import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import com.restaurant.restaurantorderingsystem.entity.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.paymentStatus = :paymentStatus AND o.status <> :cancelled")
    BigDecimal sumRevenue(@Param("paymentStatus") PaymentStatus paymentStatus, @Param("cancelled") OrderStatus cancelled);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem LEFT JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findDetailedById(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem LEFT JOIN FETCH o.customer ORDER BY o.createdAt DESC")
    List<Order> findAllDetailed();

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem LEFT JOIN FETCH o.customer WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findDetailedByCustomerId(@Param("customerId") Long customerId);
}
