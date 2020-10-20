package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.properties.SystemProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MeanAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    public static final String NAME = "aggregateMean";
    public static final String ARITHMETIC = "arithmetic";
    public static final String GEOMETRIC = "geometric";
    public static final String HARMONIC = "harmonic";
    public static final String MEDIAN = "median";

    public MeanAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length >= 1) {
            try {
                String meanKind = ARITHMETIC;
                if(parameters.length >= 2) {
                    meanKind = getParameter(1, parameters);
                }
                boolean group = true;
                if(parameters.length >= 3) {
                    group = getParameter(2, parameters);
                }
                Number meanValue;

                if(meanKind.equals(MEDIAN)) {
                    meanValue = 0;
                    List<Number> values = new ArrayList<>();
                    for (Object row : resultSet) {
                        Object value = resolveValue(row, parameters[0]);
                        if(value != null) {
                            if(value instanceof Collection){
                                values.addAll((Collection<? extends Number>) value);
                            } else if(value instanceof Number) {
                                values.add((Number) value);
                            }
                        }
                    }
                    if(values.size() == 1) {
                        meanValue = values.get(0);
                    } else if(values.size() > 1) {
                        values.sort((o1, o2) -> (int) (o1.doubleValue() - o2.doubleValue()));
                        meanValue = values.get(Math.round(values.size() / 2.0f) - 1);
                    }
                } else {
                    Number accumulatedValue = 0;
                    Number accumulatedCounter = 0;
                    Number[] functionResult;
                    for (Object row : resultSet) {
                        switch (meanKind) {
                            case GEOMETRIC: {
                                functionResult = accumulateFunction(accumulatedValue, new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.multiply(V));
                                break;
                            }
                            case HARMONIC: {
                                functionResult = accumulateFunction(accumulatedValue, new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.add(new BigDecimal(1).
                                        divide(V, SystemProperties.getInteger(SystemProperties.Query.Function.BIG_DECIMAL_DIVIDE_SCALE), RoundingMode.HALF_EVEN)));
                                break;
                            }
                            default: {
                                functionResult = accumulateFunction(accumulatedValue, new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.add(V));
                            }
                        }
                        accumulatedCounter = accumulatedCounter.doubleValue() + functionResult[0].doubleValue();
                        accumulatedValue = functionResult[1];
                    }

                    switch (meanKind) {
                        case GEOMETRIC: {
                            meanValue = Math.pow(accumulatedValue.doubleValue(), 1 / accumulatedCounter.doubleValue());
                            break;
                        }
                        case HARMONIC: {
                            meanValue = accumulatedCounter.doubleValue() / accumulatedValue.doubleValue();
                            break;
                        }
                        default: {
                            meanValue = accumulatedValue.doubleValue() / accumulatedCounter.doubleValue();
                        }
                    }
                }

                if(group) {
                    Collection<JoinableMap> newResultSet = new ArrayList<>();
                    JoinableMap mean = new JoinableMap(new HashMap<>(), alias);
                    mean.put(alias, meanValue);
                    newResultSet.add(mean);
                    result = newResultSet;
                } else {
                    for(Object row : resultSet) {
                        ((Map) row).put(alias, meanValue);
                    }
                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Mean aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Mean aggregate function need at leas two parameter");
        }
        return result;
    }
}
