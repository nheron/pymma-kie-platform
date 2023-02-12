package org.chtijbug.drools.proxy.persistence.model;


import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"name"}),
        @UniqueConstraint(columnNames={"spaceName"}),
        @UniqueConstraint(columnNames={"projectName"})
})

public class UserGroups {
    @Id
    @GeneratedValue
    private Long uniqueId;

    private String name;

    private String spaceName;

    private String projectName;

    @ManyToMany
    private KieWorkbench kieWorkbench;

    @ManyToMany
    private ProjectPersist projectPersist;

    @ManyToMany
    private UserGroups workspaceUserGroup;

    public UserGroups() {
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UserGroups( String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroups that = (UserGroups) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
