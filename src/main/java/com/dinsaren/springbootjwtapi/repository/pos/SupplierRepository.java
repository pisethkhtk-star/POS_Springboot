package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    List<Supplier> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.contact) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.createdAt DESC")
    List<Supplier> searchSuppliers(@Param("query") String query);
}
