package org.hcjf.examples.salary;

import org.hcjf.layers.Layer;
import org.hcjf.utils.Introspection;

import java.util.List;
import java.util.Map;

public class SaludCalculator extends Layer implements ItemCalculator {
    @Override
    public Map<String, Object> calculate(Map<String, Object> salary, String itemName) {
        Double rawSalary = ((Number) Introspection.resolve(salary, "rawSalary")).doubleValue();
        Double value = rawSalary * 0.07;
        Double totalSalary = ((Number) Introspection.resolve(salary, "totalSalary")).doubleValue();
        List<Map<String,Object>> items = Introspection.resolve("items");
        items.add(Map.of("name", "salud", "value", value));
        return salary;
    }
}
