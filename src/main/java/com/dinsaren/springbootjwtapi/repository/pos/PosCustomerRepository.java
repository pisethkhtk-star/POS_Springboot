package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosCustomerRepository extends JpaRepository<PosCustomer, Integer> {
    List<PosCustomer> findAllByOrderByCreatedAtDesc();
    Optional<PosCustomer> findByPhone(String phone);
    boolean existsByPhone(String phone);

    @Query("SELECT c FROM PosCustomer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY c.createdAt DESC")
    List<PosCustomer> searchCustomers(@Param("query") String query);
}
