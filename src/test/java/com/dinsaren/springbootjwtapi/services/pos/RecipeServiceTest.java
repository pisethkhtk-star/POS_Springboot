package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.models.pos.ProductIngredient;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientRepository;
import com.dinsaren.springbootjwtapi.repository.pos.PosProductRepository;
import com.dinsaren.springbootjwtapi.repository.pos.ProductIngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @Mock
    private PosProductRepository productRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private ProductIngredientRepository productIngredientRepository;

    @Mock
    private UnitConversionService unitConversionService;

    @InjectMocks
    private RecipeService recipeService;

    private PosProduct latte;
    private Ingredient espressoBeans;
    private Ingredient freshMilk;

    @BeforeEach
    void setUp() {
        latte = new PosProduct();
        latte.setId(1);
        latte.setName("Iced Latte");
        latte.setCode("LATTE-001");
        latte.setPrice(new BigDecimal("2.75"));

        // Espresso Beans: base unit is "g", cost per gram = $0.012
        espressoBeans = new Ingredient("Espresso Beans", "ING-001", "g", UnitType.WEIGHT,
                new BigDecimal("5000"), new BigDecimal("500"), new BigDecimal("0.012"));
        espressoBeans.setId(1);

        // Fresh Milk: base unit is "ml", cost per ml = $0.0022
        freshMilk = new Ingredient("Fresh Milk", "ING-002", "ml", UnitType.VOLUME,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("0.0022"));
        freshMilk.setId(2);
    }

    @Test
    void testCalculateProductCOGS() {
        // Recipe: 20g beans + 150ml milk
        ProductIngredient itemBeans = new ProductIngredient(latte, espressoBeans, new BigDecimal("20"), "g");
        ProductIngredient itemMilk = new ProductIngredient(latte, freshMilk, new BigDecimal("150"), "ml");

        when(productIngredientRepository.findByProductId(1)).thenReturn(List.of(itemBeans, itemMilk));

        when(unitConversionService.convertToBase(new BigDecimal("20"), "g", "g")).thenReturn(new BigDecimal("20"));
        when(unitConversionService.convertToBase(new BigDecimal("150"), "ml", "ml")).thenReturn(new BigDecimal("150"));

        // Beans cost: 20 * 0.012 = 0.24
        // Milk cost: 150 * 0.0022 = 0.33
        // Total expected COGS = 0.57
        BigDecimal cogs = recipeService.calculateProductCOGS(1);
        assertEquals(0, new BigDecimal("0.57").compareTo(cogs));
    }
}
