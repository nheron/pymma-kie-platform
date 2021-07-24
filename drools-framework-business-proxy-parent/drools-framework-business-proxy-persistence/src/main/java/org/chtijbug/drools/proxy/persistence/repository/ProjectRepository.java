package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.json.KieProject;
import org.chtijbug.drools.proxy.persistence.model.KieWorkbench;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<ProjectPersist, String> {

    public List<ProjectPersist> findByProjectName(KieProject projectName);
    public ProjectPersist findByProjectNameAndBranch(KieProject projectName,String branch);
    public ProjectPersist findByDeploymentName(String deploymentName);
    public ProjectPersist findByUuid(String UUID);
    public List<ProjectPersist> findByServerNamesIn(List<String> serverNames);
    public List<ProjectPersist> findByServerNamesInAndDeploymentName(List<String> serverNames,String deploymentName);
    public List<ProjectPersist> findByKieWorkbench(KieWorkbench kieWorkbench);

}
