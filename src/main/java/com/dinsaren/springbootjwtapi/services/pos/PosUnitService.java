package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.payload.pos.UnitRequest;
import com.dinsaren.springbootjwtapi.repository.pos.UnitRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosUnitService {

    private final UnitRepository unitRepository;

    @PostConstruct
    public void initDefaultUnits() {
        if (unitRepository.count() == 0) {
            log.info("Seeding initial default measurement units...");
            List<Unit> defaults = List.of(
                new Unit("Kilogram", "kg", "Standard metric mass"),
                new Unit("Gram", "g", "Metric mass for small measurements"),
                new Unit("Liter", "liter", "Standard metric volume"),
                new Unit("Milliliter", "ml", "Liquid volume for recipes"),
                new Unit("Can", "can", "Canned ingredients (milk, soda)"),
                new Unit("Bottle", "bottle", "Bottled ingredients or syrups"),
                new Unit("Pieces", "pcs", "Countable units (cups, lids)"),
                new Unit("Pack", "pack", "Packaged goods"),
                new Unit("Bag", "bag", "Bagged items (beans, sugar)"),
                new Unit("Box", "box", "Boxed goods"),
                new Unit("Cup", "cup", "Serving cups"),
                new Unit("Ounce", "oz", "Fluid/dry ounce"),
                new Unit("Tablespoon", "tbsp", "Cooking measure (~15ml)"),
                new Unit("Teaspoon", "tsp", "Cooking measure (~5ml)")
            );
            unitRepository.saveAll(defaults);
            log.info("Initialized {} default units.", defaults.size());
        }
    }

    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
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

        String symbol = request.getSymbol().trim().toLowerCase();
        Unit unit = new Unit(name, symbol, request.getDescription());
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

        if (request.getSymbol() != null && !request.getSymbol().trim().isEmpty()) {
            unit.setSymbol(request.getSymbol().trim().toLowerCase());
        }

        if (request.getDescription() != null) {
            unit.setDescription(request.getDescription().trim());
        }

        return unitRepository.save(unit);
    }

    @Transactional
    public void deleteUnit(Integer id) {
        Unit unit = getUnitById(id);
        unitRepository.delete(unit);
    }
}
