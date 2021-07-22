package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

public interface QueryConditional {

    /**
     * Return query instance that contains all the evaluators.
     * @return Query evaluation instance.
     */
    Query getEvaluationQuery();

    class ConditionalValue {

        private final String value;

        public ConditionalValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }
}
