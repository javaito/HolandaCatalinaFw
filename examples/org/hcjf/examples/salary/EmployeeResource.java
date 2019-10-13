package org.hcjf.examples.salary;

import org.hcjf.layers.Layer;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.layers.crud.ReadLayerInterface;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.distributed.DistributedLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;

import java.util.*;

public class EmployeeResource extends Layer implements CreateLayerInterface<Map<String,Object>>,
        ReadLayerInterface<Map<String,Object>>,
        ReadRowsLayerInterface,
        DistributedLayerInterface {

    private Map<UUID, Map<String,Object>> db;

    public EmployeeResource() {
        db = new HashMap<>();
    }

    @Override
    public String getImplName() {
        return "employee";
    }

    @Override
    public Map<String, Object> create(Map<String, Object> employee) {
        UUID id = UUID.randomUUID();
        employee.put("id", id);
        db.put(id, employee);
        return employee;
    }

    @Override
    public Map<String, Object> read(Object id) {
        return db.get(id);
    }

    @Override
    public Collection<JoinableMap> readRows(Queryable queryable) {
        Collection<JoinableMap> result = new ArrayList<>();
        db.values().forEach(M -> result.add(new JoinableMap(M)));
        return queryable.evaluate(result);
    }
}
