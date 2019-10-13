package org.hcjf.examples.salary;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.layers.crud.ReadLayerInterface;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.distributed.DistributedLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;
import org.hcjf.utils.Introspection;

import java.util.*;

public class SalaryResource extends Layer implements CreateLayerInterface<Map<String,Object>>,
        ReadRowsLayerInterface,
        DistributedLayerInterface {

    private Map<UUID,Map<String,Object>> db;

    public static final String SALUD = "salud";
    public static final String RETIRO = "retiro";

    public SalaryResource() {
        db = new HashMap<>();
    }

    @Override
    public String getImplName() {
        return "salary";
    }

    @Override
    public Map<String, Object> create(Map<String, Object> salary) {

        UUID employeeId = Introspection.resolve(salary, "employeeId");

        ReadLayerInterface readLayerInterface =
                Layers.get(ReadLayerInterface.class, "employee");
        if(readLayerInterface.read(employeeId) == null) {
            throw new HCJFRuntimeException("Employee not found");
        }

        Double rawSalary = ((Number)Introspection.resolve(salary, "rawSalary")).doubleValue();
        Double totalSalary = rawSalary;
        List<String> items = Introspection.resolve(salary, "items");
        List<Map<String, Object>> newItems = new ArrayList<>();
        for(String item : items) {
            ItemCalculator calculator = Layers.get(ItemCalculator.class, item);
            calculator.calculate(salary, item);
        }

        UUID id = UUID.randomUUID();
        salary.put("items", newItems);
        salary.put("totalSalary", totalSalary);
        salary.put("date", new Date());
        salary.put("id", id);
        db.put(id, salary);
        return salary;
    }

    @Override
    public Collection<JoinableMap> readRows(Queryable queryable) {
        Collection<JoinableMap> result = new ArrayList<>();
        db.values().forEach(M -> result.add(new JoinableMap(M)));
        return queryable.evaluate(result);
    }
}
