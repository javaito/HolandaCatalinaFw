package org.hcjf.layers.query.functions;

import org.hcjf.utils.Introspection;
import org.hcjf.utils.Maths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForecastFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    private static final String IMPL_NAME = "forecast";

    public ForecastFunctionLayer() {
        super(IMPL_NAME);
    }

    /**
     * Evaluates the specific function.
     * @param alias      Alias of the function
     * @param resultSet  Result set obtained for the query evaluation.
     * @param parameters Function's parameters.
     * @return Function result.
     */
    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        String independentVariablePath = getParameter(0, parameters);
        String dependentVariablePath = getParameter(1, parameters);
        Boolean polynomialRegression = true;
        Integer degree = 3;
        Double errorThreshold = 0.001;
        if(parameters.length >= 3) {
            polynomialRegression = getParameter(2, parameters);
        }

        if(polynomialRegression) {
            if(parameters.length >= 4) {
                degree = ((Number)getParameter(3, parameters)).intValue();
            }
            if(parameters.length == 5) {
                errorThreshold = ((Number)getParameter(4, parameters)).doubleValue();
            }
        }

        List<Object> objectsWithUnknowns = new ArrayList<>();
        List<Number> independentValues = new ArrayList<>();
        List<Number> dependentValues = new ArrayList<>();
        List<Number> unknowns = new ArrayList<>();

        for(Object obj : resultSet) {
            Number x = Introspection.resolve(obj, independentVariablePath);
            Number y = Introspection.resolve(obj, dependentVariablePath);

            if(x != null) {
                if (y == null) {
                    objectsWithUnknowns.add(obj);
                    unknowns.add(x);
                } else {
                    independentValues.add(x);
                    dependentValues.add(y);
                }
            }
        }

        List<Number> newDependentValues;
        if(polynomialRegression) {
            newDependentValues = Maths.polynomialRegression(independentValues, dependentValues, unknowns, degree, errorThreshold);
        } else {
            newDependentValues = Maths.linearRegression(independentValues, dependentValues, unknowns);
        }
        for (int i = 0; i < newDependentValues.size(); i++) {
            Introspection.put(objectsWithUnknowns.get(i), newDependentValues.get(i), dependentVariablePath);
        }

        return resultSet;
    }

}
