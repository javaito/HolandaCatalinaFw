package org.hcjf.cloud.impl.messages;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author javaito
 */
public class PublishObjectMessage extends Message {

    private List<Path> paths;
    private Long timestamp;
    private List<UUID> nodes;

    public PublishObjectMessage() {
    }

    public PublishObjectMessage(UUID id) {
        super(id);
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<UUID> getNodes() {
        return nodes;
    }

    public void setNodes(List<UUID> nodes) {
        this.nodes = nodes;
    }

    public static final class Path implements BsonParcelable {

        private Object[] path;
        private Object value;

        public Path() {
        }

        public Path(Object[] path) {
            this.path = path;
        }

        public Path(Object[] path, Object value) {
            this.path = path;
            this.value = value;
        }

        public Object[] getPath() {
            return path;
        }

        public void setPath(Object[] path) {
            this.path = path;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
