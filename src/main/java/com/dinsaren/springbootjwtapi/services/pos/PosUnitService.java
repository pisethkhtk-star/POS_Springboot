package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.payload.pos.UnitRequest;

import java.util.List;

public interface PosUnitService {
    List<Unit> getAllUnits();
    List<Unit> getUnitsByType(UnitType unitType);
    Unit getUnitById(Integer id);
    Unit createUnit(UnitRequest request);
    Unit updateUnit(Integer id, UnitRequest request);
    void deleteUnit(Integer id);
}
