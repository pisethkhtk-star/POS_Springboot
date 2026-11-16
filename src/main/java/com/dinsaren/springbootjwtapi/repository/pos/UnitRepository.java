package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    Optional<Unit> findByNameIgnoreCase(String name);
    Optional<Unit> findBySymbolIgnoreCase(String symbol);
    Optional<Unit> findByUnitTypeAndIsBaseTrue(UnitType unitType);
    List<Unit> findByUnitType(UnitType unitType);
    boolean existsByNameIgnoreCase(String name);
}
