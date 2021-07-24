package org.chtijbug.drools.proxy.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.security.PrivateKey;

@Document
public class ContainerPojoPersist {

    @Id
    private String id;

    private boolean disableRuleLogging;

    @Indexed
    private String className;

    @Indexed
    private String containerId;
    @Indexed
    private String serverName;

    private String processID;

    private String projectName;

    private String groupId;

    private String artifactId;

    private String version;

    private String projectUUID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ContainerPojoPersist{");
        sb.append("id='").append(id).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", containerId='").append(containerId).append('\'');
        sb.append(", serverName='").append(serverName).append('\'');
        sb.append(", processID='").append(processID).append('\'');
        sb.append(", projectName='").append(projectName).append('\'');
        sb.append(", groupId='").append(groupId).append('\'');
        sb.append(", artifactId='").append(artifactId).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
