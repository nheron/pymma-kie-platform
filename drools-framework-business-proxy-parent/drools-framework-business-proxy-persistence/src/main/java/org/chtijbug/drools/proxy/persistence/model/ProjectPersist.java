package org.chtijbug.drools.proxy.persistence.model;

import org.chtijbug.drools.proxy.persistence.json.KieProject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Document
@CompoundIndexes({
        @CompoundIndex(def = "{'projectName':1, 'branch':1}", name = "projectName_branch_Index")
})
public class ProjectPersist  implements Serializable {

    public static final String ADEFINIR="A définir";

    public static final String DEFINI="Défini";

    public static final String Deployable="Déployable";

    @Indexed
    private String deploymentName;

    @Id
    @Indexed
    private String uuid;

    @DBRef
    private KieWorkbench kieWorkbench;


    private KieProject projectName;

    private String mainClass;

    private String groupID;

    private String artifactID;

    private String processID;

    private String projectVersion;

    private String containerID;

    private String branch;

    private List<String> serverNames= new ArrayList<>();

    private String status;

    private List<String> classNameList;

    public ProjectPersist(){}

    public ProjectPersist(String deploymentName, KieProject projectName, String mainClass, String groupID, String artifactID, String processID, String projectVersion, String containerID, List<String> serverNames, String status) {
        this.deploymentName = deploymentName;
        this.projectName = projectName;
        this.mainClass = mainClass;
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.processID = processID;
        this.projectVersion = projectVersion;
        this.containerID = containerID;
        this.serverNames = serverNames;
        this.status = status;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public KieProject getProjectName() {
        return projectName;
    }

    public void setProjectName(KieProject projectName) {
        this.projectName = projectName;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getArtifactID() {
        return artifactID;
    }

    public void setArtifactID(String artifactID) {
        this.artifactID = artifactID;
    }

    public String getProcessID() {
        return processID;
    }

    public String getContainerID() {
        return containerID;
    }

    public void setContainerID(String containerID) {
        this.containerID = containerID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public List<String> getServerNames() {
        return serverNames;
    }

    public void setServerName(List<String> serverNames) {
        this.serverNames = serverNames;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getClassNameList() {
        return classNameList;
    }

    public void setClassNameList(List<String> classNameList) {
        this.classNameList = classNameList;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public KieWorkbench getKieWorkbench() {
        return kieWorkbench;
    }

    public void setKieWorkbench(KieWorkbench kieWorkbench) {
        this.kieWorkbench = kieWorkbench;
    }

    public void setServerNames(List<String> serverNames) {
        this.serverNames = serverNames;
    }

    public String getWorkspaceName(){
        if (this.projectName!= null){
            return projectName.getSpaceName();
        }
        return null;
    }
    public String getKieProjectName(){
        if (this.projectName!= null){
            return projectName.getName()+"-"+this.branch;
        }
        return null;
    }
    public ProjectPersist duplicate(){
        ArrayList<String> listServerNames = new ArrayList<String>();
        listServerNames.addAll(serverNames);
        ProjectPersist duplicate = new ProjectPersist(deploymentName,projectName,mainClass,groupID,artifactID,processID,projectVersion,containerID,listServerNames,status);
        return duplicate;
    }
}
