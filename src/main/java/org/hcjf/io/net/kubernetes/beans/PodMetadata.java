package org.hcjf.io.net.kubernetes.beans;

import java.util.Map;
import java.util.UUID;

/**
 * @author javaito
 */
public class PodMetadata {

    public static final class Fields {
        public static final String UID = "uid";
        public static final String NAME = "name";
        public static final String GENERATE_NAME = "generateName";
        public static final String NAMESPACE = "namespace";
        public static final String SELF_LINK = "selfLink";
        public static final String RESOURCE_VERSION = "resourceVersion";
        public static final String CREATION_TIMESTAMP = "creationTimestamp";
        public static final String LABELS = "labels";
        public static final String ANNOTATIONS = "annotations";
    }

    private UUID uid;
    private String name;
    private String generateName;
    private String namespace;
    private String selfLink;
    private String resourceVersion;
    private Long creationTimestamp;
    private Map<String,String> labels;
    private Map<String,String> annotations;

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenerateName() {
        return generateName;
    }

    public void setGenerateName(String generateName) {
        this.generateName = generateName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }
}
