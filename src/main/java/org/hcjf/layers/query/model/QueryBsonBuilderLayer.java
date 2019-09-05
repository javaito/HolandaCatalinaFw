package org.hcjf.layers.query.model;

import org.hcjf.bson.BsonDocument;
import org.hcjf.layers.Layer;
import org.hcjf.layers.query.Query;
import org.hcjf.utils.bson.BsonCustomBuilderLayer;

/**
 * This inner class implements the custom method to create a query instance from a bson document.
 */
public class QueryBsonBuilderLayer extends Layer implements BsonCustomBuilderLayer<Query> {

    public QueryBsonBuilderLayer() {
        super(Query.class.getName());
    }

    /**
     * This implementation required that the document contains a field called '__query__' to create the query instance.
     * @param document Bson document.
     * @return Returns a query instance.
     */
    @Override
    public Query create(BsonDocument document) {
        return Query.compile(document.get(Query.QUERY_BSON_FIELD_NAME).getAsString());
    }

}
