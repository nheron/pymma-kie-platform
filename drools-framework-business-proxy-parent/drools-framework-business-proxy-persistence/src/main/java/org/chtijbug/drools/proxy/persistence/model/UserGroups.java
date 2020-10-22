package org.chtijbug.drools.proxy.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserGroups {
    @Id
    @Indexed
    private String ID;
    @Indexed
    private String name;
    @Indexed
    private String spaceName;
    @Indexed
    private String projectName;

    @DBRef
    private KieWorkbench kieWorkbench;

    @DBRef
    private ProjectPersist projectPersist;

    @DBRef
    private UserGroups workspaceUserGroup;

    public UserGroups() {
    }

    public UserGroups(String ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public KieWorkbench getKieWorkbench() {
        return kieWorkbench;
    }

    public void setKieWorkbench(KieWorkbench kieWorkbench) {
        this.kieWorkbench = kieWorkbench;
    }

    public ProjectPersist getProjectPersist() {
        return projectPersist;
    }

    public void setProjectPersist(ProjectPersist projectPersist) {
        this.projectPersist = projectPersist;
    }

    public UserGroups getWorkspaceUserGroup() {
        return workspaceUserGroup;
    }

    public void setWorkspaceUserGroup(UserGroups workspaceUserGroup) {
        this.workspaceUserGroup = workspaceUserGroup;
    }

}
