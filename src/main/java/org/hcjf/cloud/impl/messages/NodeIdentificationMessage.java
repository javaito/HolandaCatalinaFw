package org.hcjf.cloud.impl.messages;

import java.util.Date;
import java.util.UUID;

/**
 * @author javaito
 */
public class NodeIdentificationMessage extends Message {

    private String name;
    private String version;
    private Date startupDate;

    public NodeIdentificationMessage() {
        super(UUID.randomUUID());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getStartupDate() {
        return startupDate;
    }

    public void setStartupDate(Date startupDate) {
        this.startupDate = startupDate;
    }
}
