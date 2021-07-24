package org.chtijbug.drools.proxy.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ContainerRuntimePojoPersist {
    public enum STATUS {
        UP,
        DOWN,
        TODEPLOY,
        TODELETE
    }
    @Id
    private String id;

    @Indexed
    private String containerId;
    @Indexed
    private String serverName;
    @Indexed
    private String hostname;

    private String status;

    private String projectUUID;

    private boolean disableRuleLogging;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectUUID() {
        return projectUUID;
    }

    public void setProjectUUID(String projectUUID) {
        this.projectUUID = projectUUID;
    }

    public boolean isDisableRuleLogging() {
        return disableRuleLogging;
    }

    public void setDisableRuleLogging(boolean disableRuleLogging) {
        this.disableRuleLogging = disableRuleLogging;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ContainerRuntimePojoPersist{");
        sb.append("id='").append(id).append('\'');
        sb.append(", containerId='").append(containerId).append('\'');
        sb.append(", serverName='").append(serverName).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
