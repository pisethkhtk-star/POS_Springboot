package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosProductRepository extends JpaRepository<PosProduct, Integer> {
    List<PosProduct> findAllByOrderByNameAsc();
    List<PosProduct> findByCategoryIdOrderByNameAsc(Integer categoryId);
    Optional<PosProduct> findByCode(String code);
    boolean existsByCode(String code);
    long countByCategoryId(Integer categoryId);

    @Query("SELECT p FROM PosProduct p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.name ASC")
    List<PosProduct> searchByNameOrCode(@Param("query") String query);

    @Query("SELECT p FROM PosProduct p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.categoryId = :categoryId ORDER BY p.name ASC")
    List<PosProduct> searchByNameOrCodeAndCategoryId(@Param("query") String query, @Param("categoryId") Integer categoryId);
}
