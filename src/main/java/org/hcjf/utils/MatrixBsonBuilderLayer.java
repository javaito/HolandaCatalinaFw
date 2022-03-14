package org.hcjf.utils;

import org.hcjf.bson.BsonArray;
import org.hcjf.bson.BsonDocument;
import org.hcjf.layers.Layer;
import org.hcjf.utils.bson.BsonCustomBuilderLayer;

public class MatrixBsonBuilderLayer extends Layer implements BsonCustomBuilderLayer<Matrix> {

    public MatrixBsonBuilderLayer() {
        super(Matrix.class.getName());
    }

    /**
     * Returns a parcelable instance from a bson document.
     *
     * @param document Bson document.
     * @return Bson parcelable instance.
     */
    @Override
    public Matrix create(BsonDocument document) {
        Integer rows = document.get(Matrix.Fields.ROWS).getAsInteger();
        Integer cols = document.get(Matrix.Fields.COLS).getAsInteger();
        BsonArray values = document.get(Matrix.Fields.DATA).getAsArray();
        return new Matrix(rows, cols, values.toList().toArray(new Number[]{}));
    }
}
