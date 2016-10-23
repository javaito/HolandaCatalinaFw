package org.hcjf.view.components;

import org.hcjf.view.ViewComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewDataSet extends ViewComponent {

    private final List<Map<String, Object>> dataSet;

    public ViewDataSet(String name) {
        super(name);
        dataSet = new ArrayList<>();
    }

    public List<Map<String,Object>> getDataSet() {
        return dataSet;
    }

    public void add(Map<String,Object> entry){
        dataSet.add(entry);
    }
}
