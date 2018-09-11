package org.hcjf.io.net.kubernetes.beans;

import java.util.List;

/**
 * @author javaito
 */
public class PodSpecContainer extends KubernetesBean {

    private String name;
    private String image;
    private String terminationMessagePath;
    private String terminationMessagePolicy;
    private String imagePullPolicy;
    private List<PodSpecContainerPort> ports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTerminationMessagePath() {
        return terminationMessagePath;
    }

    public void setTerminationMessagePath(String terminationMessagePath) {
        this.terminationMessagePath = terminationMessagePath;
    }

    public String getTerminationMessagePolicy() {
        return terminationMessagePolicy;
    }

    public void setTerminationMessagePolicy(String terminationMessagePolicy) {
        this.terminationMessagePolicy = terminationMessagePolicy;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public List<PodSpecContainerPort> getPorts() {
        return ports;
    }

    public void setPorts(List<PodSpecContainerPort> ports) {
        this.ports = ports;
    }
}
