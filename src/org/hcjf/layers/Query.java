package org.hcjf.layers;

import java.util.UUID;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Query {

    private QueryId id;
    private Integer limit;


    public Query(){
    }

    public QueryId getId() {
        return id;
    }

    public static final class QueryId {

        private final UUID id;

        public QueryId(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }
}
