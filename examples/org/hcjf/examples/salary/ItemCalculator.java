package org.hcjf.examples.salary;

import org.hcjf.layers.LayerInterface;

import java.util.Map;

public interface ItemCalculator extends LayerInterface {

    Map<String,Object> calculate(Map<String,Object> salary, String itemName);

}
