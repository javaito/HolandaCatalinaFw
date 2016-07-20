package org.hcjf.layers.storage.actions;

import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class MapResultSet extends ResultSet<Map<String, Object>> {

    public MapResultSet(Map<String, Object> result) {
        super(result.size(), result);
    }

}
