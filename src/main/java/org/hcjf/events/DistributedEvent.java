package org.hcjf.events;

import org.hcjf.utils.bson.BsonParcelable;

/**
 * @author javaito
 */
public interface DistributedEvent extends Event, BsonParcelable {

    /**
     * If the event is private sending only for an other replica of the same service.
     * @return Private value.
     */
    default Boolean isPrivate() {
        return false;
    }

    /**
     * If the event is broadcasting then the event is sending far all the replicas.
     * @return Broadcasting value.
     */
    default Boolean isBroadcasting() {
        return false;
    }

}
