/**
 * Class description
 * Author: @brunolevidev
 * Created on: 05/03/24
 * Last modified: 05/03/24
 */

package org.hcjf.layers.query.functions;

import java.util.Calendar;

public class GetNumberOfWeekQueryFunctionLayer extends BaseQueryFunctionLayer {
    private static final String NAME = "getNumberOfWeek";

    public GetNumberOfWeekQueryFunctionLayer() {
        super(NAME);
    }

    private int getNumberOfWeek(Long datetime) {
        Calendar calInstance = Calendar.getInstance();
        calInstance.setTimeInMillis(datetime);
        return calInstance.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Long datetime = getParameter(0, parameters);
        int weekNumber = getNumberOfWeek(datetime);
        return weekNumber;
    }
}
