package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.repository.pos.UnitRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitConversionService {

    private final UnitRepository unitRepository;

    @PostConstruct
    public void seedStandardUnits() {
        if (unitRepository.count() == 0) {
            log.info("Initializing standard Unit of Measure matrix (WEIGHT, VOLUME, COUNT)...");
            List<Unit> defaults = List.of(
                // WEIGHT (Base: Gram)
                new Unit(UnitType.WEIGHT, "Gram", "ក្រាម", "g", true, BigDecimal.ONE, "Base metric unit for weight"),
                new Unit(UnitType.WEIGHT, "Kilogram", "គីឡូក្រាម", "kg", false, new BigDecimal("1000"), "1 Kilogram = 1,000 Grams"),
                new Unit(UnitType.WEIGHT, "Ounce", "អោនស៍", "oz", false, new BigDecimal("28.3495"), "1 Ounce = 28.3495 Grams"),
                new Unit(UnitType.WEIGHT, "Pound", "ផោន", "lb", false, new BigDecimal("453.592"), "1 Pound = 453.592 Grams"),

                // VOLUME (Base: Milliliter)
                new Unit(UnitType.VOLUME, "Milliliter", "មីលីលីត្រ", "ml", true, BigDecimal.ONE, "Base metric unit for volume"),
                new Unit(UnitType.VOLUME, "Liter", "លីត្រ", "L", false, new BigDecimal("1000"), "1 Liter = 1,000 Milliliters"),
                new Unit(UnitType.VOLUME, "Tablespoon", "ស្លាបព្រាបាយ", "tbsp", false, new BigDecimal("15"), "1 Tablespoon = 15 Milliliters"),
                new Unit(UnitType.VOLUME, "Teaspoon", "ស្លាបព្រាកាហ្វេ", "tsp", false, new BigDecimal("5"), "1 Teaspoon = 5 Milliliters"),
                new Unit(UnitType.VOLUME, "Cup", "កែវរង្វាស់", "cup", false, new BigDecimal("240"), "1 Measuring Cup = 240 Milliliters"),
                new Unit(UnitType.VOLUME, "Fluid Ounce", "អោនស៍រាវ", "fl oz", false, new BigDecimal("29.5735"), "1 Fluid Ounce = 29.5735 Milliliters"),

                // COUNT (Base: Piece)
                new Unit(UnitType.COUNT, "Piece", "ដុំ / ផ្លែ", "pcs", true, BigDecimal.ONE, "Base unit for countable items"),
                new Unit(UnitType.COUNT, "Dozen", "ដប់ពីរ", "dz", false, new BigDecimal("12"), "1 Dozen = 12 Pieces"),
                new Unit(UnitType.COUNT, "Can", "កំប៉ុង", "can", false, BigDecimal.ONE, "Single can container"),
                new Unit(UnitType.COUNT, "Bottle", "ដប", "bottle", false, BigDecimal.ONE, "Single bottle container"),
                new Unit(UnitType.COUNT, "Pack", "កញ្ចប់", "pack", false, BigDecimal.ONE, "Single package"),
                new Unit(UnitType.COUNT, "Box", "ប្រអប់", "box", false, BigDecimal.ONE, "Single box")
            );
            unitRepository.saveAll(defaults);
            log.info("Successfully seeded {} standard units across WEIGHT, VOLUME, and COUNT.", defaults.size());
        }
    }

    /**
     * Convert quantity between two units of the same UnitType.
     * Throws IllegalArgumentException if unit types do not match.
     */
    public BigDecimal convert(BigDecimal quantity, Unit fromUnit, Unit toUnit) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (fromUnit == null || toUnit == null) {
            throw new IllegalArgumentException("Both fromUnit and toUnit must not be null");
        }
        if (!fromUnit.getUnitType().equals(toUnit.getUnitType())) {
            throw new IllegalArgumentException("Incompatible unit conversion: Cannot convert " +
                    fromUnit.getUnitType() + " (" + fromUnit.getSymbol() + ") to " +
                    toUnit.getUnitType() + " (" + toUnit.getSymbol() + ")");
        }

        if (fromUnit.getSymbol().equalsIgnoreCase(toUnit.getSymbol())) {
            return quantity;
        }

        // 1. Convert to base unit: qty * fromFactor
        BigDecimal baseQty = quantity.multiply(fromUnit.getFactorToBase());

        // 2. Convert from base to target unit: baseQty / toFactor
        return baseQty.divide(toUnit.getFactorToBase(), 6, RoundingMode.HALF_UP);
    }

    /**
     * Convert quantity from a given unit directly into its base unit.
     */
    public BigDecimal convertToBase(BigDecimal quantity, Unit fromUnit) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (fromUnit == null) {
            return quantity;
        }
        return quantity.multiply(fromUnit.getFactorToBase()).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * Convert quantity to base unit using unit symbols.
     * If units are identical or target base is the same, handles conversion gracefully.
     */
    public BigDecimal convertToBase(BigDecimal quantity, String fromSymbol, String baseSymbol) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (fromSymbol == null || baseSymbol == null || fromSymbol.equalsIgnoreCase(baseSymbol)) {
            return quantity;
        }

        Optional<Unit> fromOpt = unitRepository.findBySymbolIgnoreCase(fromSymbol);
        Optional<Unit> baseOpt = unitRepository.findBySymbolIgnoreCase(baseSymbol);

        if (fromOpt.isPresent() && baseOpt.isPresent()) {
            return convert(quantity, fromOpt.get(), baseOpt.get());
        }

        // Fallback for case variations or custom symbols
        log.warn("Could not find unit mapping for symbols '{}' and '{}'. Assuming 1:1 conversion.", fromSymbol, baseSymbol);
        return quantity;
    }

    /**
     * Convert an ingredient's custom purchase unit quantity (e.g. 2 bags, 5 boxes) to ingredient's base unit.
     */
    public BigDecimal convertPurchaseUnitToBase(BigDecimal purchaseQty, IngredientPurchaseUnit purchaseUnit) {
        if (purchaseQty == null || purchaseQty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (purchaseUnit == null) {
            return purchaseQty;
        }
        return purchaseQty.multiply(purchaseUnit.getFactorToBase()).setScale(6, RoundingMode.HALF_UP);
    }

    public Optional<Unit> findBaseUnitForType(UnitType unitType) {
        return unitRepository.findByUnitTypeAndIsBaseTrue(unitType);
    }

    public Optional<Unit> findBySymbol(String symbol) {
        return unitRepository.findBySymbolIgnoreCase(symbol);
    }
}
