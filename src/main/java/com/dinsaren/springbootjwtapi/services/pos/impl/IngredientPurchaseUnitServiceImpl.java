package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientPurchaseUnitRequest;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientPurchaseUnitRepository;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientRepository;
import com.dinsaren.springbootjwtapi.services.pos.IngredientPurchaseUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientPurchaseUnitServiceImpl implements IngredientPurchaseUnitService {

    private final IngredientPurchaseUnitRepository purchaseUnitRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IngredientPurchaseUnit> getUnits(Integer ingredientId) {
        return purchaseUnitRepository.findByIngredientId(ingredientId);
    }

    @Override
    @Transactional
    public IngredientPurchaseUnit addUnit(Integer ingredientId, IngredientPurchaseUnitRequest request) {
        ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found: " + ingredientId));

        if (request.getUnitName() == null || request.getUnitName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit name is required");
        }
        if (request.getFactorToBase() == null || request.getFactorToBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "factorToBase must be greater than zero");
        }

        IngredientPurchaseUnit pu = new IngredientPurchaseUnit(
                ingredientId,
                request.getUnitName().trim(),
                request.getUnitNameKm(),
                request.getFactorToBase(),
                request.getDefaultCost()
        );

        return purchaseUnitRepository.save(pu);
    }

    @Override
    @Transactional
    public void deleteUnit(Integer ingredientId, Integer unitId) {
        IngredientPurchaseUnit pu = purchaseUnitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase unit not found"));
        purchaseUnitRepository.delete(pu);
    }
}
