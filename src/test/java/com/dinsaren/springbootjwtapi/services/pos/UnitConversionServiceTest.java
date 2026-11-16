package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.repository.pos.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UnitConversionServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UnitConversionService unitConversionService;

    private Unit gram;
    private Unit kilogram;
    private Unit milliliter;
    private Unit liter;
    private Unit piece;
    private Unit dozen;

    @BeforeEach
    void setUp() {
        gram = new Unit(UnitType.WEIGHT, "Gram", "ក្រាម", "g", true, BigDecimal.ONE);
        kilogram = new Unit(UnitType.WEIGHT, "Kilogram", "គីឡូក្រាម", "kg", false, new BigDecimal("1000"));

        milliliter = new Unit(UnitType.VOLUME, "Milliliter", "មីលីលីត្រ", "ml", true, BigDecimal.ONE);
        liter = new Unit(UnitType.VOLUME, "Liter", "លីត្រ", "L", false, new BigDecimal("1000"));

        piece = new Unit(UnitType.COUNT, "Piece", "ដុំ / ផ្លែ", "pcs", true, BigDecimal.ONE);
        dozen = new Unit(UnitType.COUNT, "Dozen", "ដប់ពីរ", "dz", false, new BigDecimal("12"));
    }

    @Test
    void testConvertKilogramToGram() {
        BigDecimal result = unitConversionService.convert(new BigDecimal("1.5"), kilogram, gram);
        assertEquals(0, new BigDecimal("1500.000000").compareTo(result));
    }

    @Test
    void testConvertGramToKilogram() {
        BigDecimal result = unitConversionService.convert(new BigDecimal("500"), gram, kilogram);
        assertEquals(0, new BigDecimal("0.500000").compareTo(result));
    }

    @Test
    void testConvertLiterToMilliliter() {
        BigDecimal result = unitConversionService.convert(new BigDecimal("2.25"), liter, milliliter);
        assertEquals(0, new BigDecimal("2250.000000").compareTo(result));
    }

    @Test
    void testConvertDozenToPiece() {
        BigDecimal result = unitConversionService.convert(new BigDecimal("3"), dozen, piece);
        assertEquals(0, new BigDecimal("36.000000").compareTo(result));
    }

    @Test
    void testIncompatibleUnitTypesThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            unitConversionService.convert(BigDecimal.TEN, kilogram, liter)
        );
        assertTrue(ex.getMessage().contains("Incompatible unit conversion"));
    }

    @Test
    void testConvertPurchaseUnitToBase() {
        IngredientPurchaseUnit bagOfBeans = new IngredientPurchaseUnit(1, "Bag", "បាវ", new BigDecimal("1000"), new BigDecimal("12.00"));
        BigDecimal baseQuantity = unitConversionService.convertPurchaseUnitToBase(new BigDecimal("5"), bagOfBeans);
        assertEquals(0, new BigDecimal("5000.000000").compareTo(baseQuantity));
    }

    @Test
    void testConvertToBaseBySymbol() {
        when(unitRepository.findBySymbolIgnoreCase("kg")).thenReturn(Optional.of(kilogram));
        when(unitRepository.findBySymbolIgnoreCase("g")).thenReturn(Optional.of(gram));

        BigDecimal result = unitConversionService.convertToBase(new BigDecimal("2.5"), "kg", "g");
        assertEquals(0, new BigDecimal("2500.000000").compareTo(result));
    }
}
