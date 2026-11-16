package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.payload.pos.UnitRequest;
import com.dinsaren.springbootjwtapi.repository.pos.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosUnitService {

    private final UnitRepository unitRepository;
    private final UnitConversionService unitConversionService;

    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    public List<Unit> getUnitsByType(UnitType unitType) {
        return unitRepository.findByUnitType(unitType);
    }

    public Unit getUnitById(Integer id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found with id: " + id));
    }

    @Transactional
    public Unit createUnit(UnitRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Unit name is required");
        }
        if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
            throw new RuntimeException("Unit symbol is required");
        }

        String name = request.getName().trim();
        if (unitRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("A unit with name '" + name + "' already exists");
        }

        UnitType unitType = request.getUnitType() != null ? request.getUnitType() : UnitType.COUNT;
        boolean isBase = Boolean.TRUE.equals(request.getIsBase());
        BigDecimal factor = request.getFactorToBase() != null && request.getFactorToBase().compareTo(BigDecimal.ZERO) > 0
                ? request.getFactorToBase()
                : BigDecimal.ONE;

        if (isBase) {
            factor = BigDecimal.ONE;
            // Ensure no other unit is marked base for this unit type
            unitRepository.findByUnitTypeAndIsBaseTrue(unitType).ifPresent(oldBase -> {
                oldBase.setIsBase(false);
                unitRepository.save(oldBase);
            });
        }

        Unit unit = new Unit(
            unitType,
            name,
            request.getNameKm(),
            request.getSymbol().trim(),
            isBase,
            factor,
            request.getDescription()
        );
        return unitRepository.save(unit);
    }

    @Transactional
    public Unit updateUnit(Integer id, UnitRequest request) {
        Unit unit = getUnitById(id);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String name = request.getName().trim();
            unitRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("A unit with name '" + name + "' already exists");
                }
            });
            unit.setName(name);
        }

        if (request.getNameKm() != null) {
            unit.setNameKm(request.getNameKm().trim());
        }

        if (request.getSymbol() != null && !request.getSymbol().trim().isEmpty()) {
            unit.setSymbol(request.getSymbol().trim());
        }

        if (request.getUnitType() != null) {
            unit.setUnitType(request.getUnitType());
        }

        if (request.getFactorToBase() != null && request.getFactorToBase().compareTo(BigDecimal.ZERO) > 0) {
            unit.setFactorToBase(request.getFactorToBase());
        }

        if (request.getIsBase() != null) {
            boolean isBase = request.getIsBase();
            if (isBase && !Boolean.TRUE.equals(unit.getIsBase())) {
                unit.setFactorToBase(BigDecimal.ONE);
                unitRepository.findByUnitTypeAndIsBaseTrue(unit.getUnitType()).ifPresent(oldBase -> {
                    if (!oldBase.getId().equals(id)) {
                        oldBase.setIsBase(false);
                        unitRepository.save(oldBase);
                    }
                });
            }
            unit.setIsBase(isBase);
        }

        if (request.getDescription() != null) {
            unit.setDescription(request.getDescription().trim());
        }

        return unitRepository.save(unit);
    }

    @Transactional
    public void deleteUnit(Integer id) {
        Unit unit = getUnitById(id);
        if (Boolean.TRUE.equals(unit.getIsBase())) {
            throw new RuntimeException("Cannot delete base unit for " + unit.getUnitType() + ". Set another unit as base first.");
        }
        unitRepository.delete(unit);
    }
}
