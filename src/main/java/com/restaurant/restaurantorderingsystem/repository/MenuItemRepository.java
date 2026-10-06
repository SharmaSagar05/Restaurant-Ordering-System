package com.restaurant.restaurantorderingsystem.repository;

import com.restaurant.restaurantorderingsystem.entity.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByActiveTrue();

    List<MenuItem> findByActiveTrueAndAvailableQuantityGreaterThan(Integer quantity);

    @Query("""
            SELECT m FROM MenuItem m
            WHERE m.active = true
              AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<MenuItem> searchActive(@Param("keyword") String keyword);

    @Query("""
            SELECT m FROM MenuItem m
            WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<MenuItem> searchAll(@Param("keyword") String keyword);
}
