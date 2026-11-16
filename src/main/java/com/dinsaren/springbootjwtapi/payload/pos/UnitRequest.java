package com.dinsaren.springbootjwtapi.payload.pos;

import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitRequest {
    private UnitType unitType = UnitType.COUNT;
    private String name;
    private String nameKm;
    private String symbol;
    private Boolean isBase = false;
    private BigDecimal factorToBase = BigDecimal.ONE;
    private String description;
}
