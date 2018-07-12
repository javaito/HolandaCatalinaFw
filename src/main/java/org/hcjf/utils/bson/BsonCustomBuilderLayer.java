package org.hcjf.utils.bson;

import org.hcjf.bson.BsonDocument;
import org.hcjf.layers.LayerInterface;

/**
 * This kind of layers provides a custom way to create a instance from a bson document.
 * @author javaito
 */
public interface BsonCustomBuilderLayer<P extends BsonParcelable> extends LayerInterface {

    /**
     * Returns a parcelable instance from a bson document.
     * @param document Bson document.
     * @return Bson parcelable instance.
     */
    P create(BsonDocument document);

}
