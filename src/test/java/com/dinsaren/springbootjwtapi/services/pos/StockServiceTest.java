package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private PosProductRepository productRepository;

    @Mock
    private ProductIngredientRepository productIngredientRepository;

    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @Mock
    private IngredientPurchaseUnitRepository purchaseUnitRepository;

    @Mock
    private UnitConversionService unitConversionService;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private StockService stockService;

    private Ingredient beans;

    @BeforeEach
    void setUp() {
        // Base stock: 1000g @ $0.010 per gram (Value = $10.00)
        beans = new Ingredient("Espresso Beans", "ING-001", "g", UnitType.WEIGHT,
                new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("0.010"));
        beans.setId(1);
    }

    @Test
    void testWeightedAverageCostOnPurchase() {
        // Current: 1000g @ $0.010 = $10.00
        // New Purchase: 2 Bags (each 1000g, total 2000g) for total cost $32.00 ($0.016 / g)
        // Expected total stock = 3000g
        // Expected total value = 10 + 32 = $42.00
        // Expected weighted average cost = 42 / 3000 = $0.014 / g

        IngredientPurchaseUnit bagUnit = new IngredientPurchaseUnit(1, "Bag", "បាវ", new BigDecimal("1000"), new BigDecimal("16.00"));
        bagUnit.setId(10);

        when(ingredientRepository.findById(1)).thenReturn(Optional.of(beans));
        when(purchaseUnitRepository.findById(10)).thenReturn(Optional.of(bagUnit));
        when(unitConversionService.convertPurchaseUnitToBase(new BigDecimal("2"), bagUnit)).thenReturn(new BigDecimal("2000"));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ingredient updated = stockService.processPurchaseIntake(1, 10, new BigDecimal("2"), new BigDecimal("32.00"), "Batch PO-1", 1);

        assertEquals(0, new BigDecimal("3000.0000").compareTo(updated.getStock()));
        assertEquals(0, new BigDecimal("0.014000").compareTo(updated.getCost()));

        verify(recipeService).recalculateAllProductsUsingIngredient(1);
    }

    @Test
    void testLowStockAlertDetection() {
        Ingredient lowStockItem = new Ingredient("Sugar", "ING-005", "g", UnitType.WEIGHT,
                new BigDecimal("300"), new BigDecimal("500"), new BigDecimal("0.0015"));
        lowStockItem.setId(5);

        when(ingredientRepository.findAll()).thenReturn(List.of(beans, lowStockItem));

        List<Ingredient> alerts = stockService.getLowStockIngredients();
        assertEquals(1, alerts.size());
        assertEquals("Sugar", alerts.get(0).getName());
    }
}
