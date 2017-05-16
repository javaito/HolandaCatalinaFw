package org.hcjf.layers.storage.actions;

import java.util.List;
import java.util.Map;

/**
 * @author javaito
 *
 */
public class MapResultSet extends ResultSet<List<Map<String, Object>>> {

    public MapResultSet(List<Map<String, Object>> result) {
        super(result.size(), result);
    }

}
