package org.hcjf.cloud.impl.messages;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author javaito
 */
public class PublishObjectMessage extends Message {

    private List<Path> paths;
    private Long timestamp;

    public PublishObjectMessage() {
        this.paths = new ArrayList<>();
    }

    public PublishObjectMessage(UUID id) {
        super(id);
        this.paths = new ArrayList<>();
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

    public static final class Path implements BsonParcelable {

        private Object[] path;
        private Object value;
        private List<UUID> nodes;

        public Path() {
        }

        public Path(Object[] path) {
            this.path = path;
        }

        public Path(Object[] path, List<UUID> nodes) {
            this.path = path;
            this.nodes = nodes;
        }

        public Path(Object[] path, Object value) {
            this.path = path;
            this.value = value;
        }

        public Path(Object[] path, Object value, List<UUID> nodes) {
            this.path = path;
            this.value = value;
            this.nodes = nodes;
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

        public List<UUID> getNodes() {
            return nodes;
        }

        public void setNodes(List<UUID> nodes) {
            this.nodes = nodes;
        }

        @Override
        public String toString() {
            return String.format("{path:%s,value:%s,node:%s}",
                    path == null ? "null" : Arrays.toString(path),
                    value == null ? "null" : value.toString(),
                    nodes == null ? "null" : Arrays.toString(nodes.toArray()));
        }
    }
}
