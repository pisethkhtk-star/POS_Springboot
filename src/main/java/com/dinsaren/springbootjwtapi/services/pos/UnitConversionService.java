package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;

import java.math.BigDecimal;
import java.util.Optional;

public interface UnitConversionService {
    void seedStandardUnits();
    BigDecimal convert(BigDecimal quantity, Unit fromUnit, Unit toUnit);
    BigDecimal convertToBase(BigDecimal quantity, Unit fromUnit);
    BigDecimal convertToBase(BigDecimal quantity, String fromSymbol, String baseSymbol);
    BigDecimal convertPurchaseUnitToBase(BigDecimal purchaseQty, IngredientPurchaseUnit purchaseUnit);
    Optional<Unit> findBaseUnitForType(UnitType unitType);
    Optional<Unit> findBySymbol(String symbol);
}
