package org.hcjf.cloud.impl.network;

import com.google.gson.Gson;
import org.hcjf.service.ServiceConsumer;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author javaito
 */
public class NetworkComponent implements ServiceConsumer, BsonParcelable {

    private UUID id;
    private String name;
    private Map<String,Object> kubernetesMetadata;

    private static final Gson gson;

    static {
        gson = new Gson();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getKubernetesMetadata() {
        return kubernetesMetadata;
    }

    public void setKubernetesMetadata(Map<String, Object> kubernetesMetadata) {
        this.kubernetesMetadata = kubernetesMetadata;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
