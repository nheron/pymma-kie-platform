package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.ContainerRuntimePojoPersist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerRuntimeRepository extends MongoRepository<ContainerRuntimePojoPersist, String> {


    List<ContainerRuntimePojoPersist> findByServerNameAndContainerId(String serverName, String containerId);
    List<ContainerRuntimePojoPersist> findByServerNameAndStatus(String serverName, String status);
    List<ContainerRuntimePojoPersist> findByServerNameAndStatusAndHostname(String serverName, String status,String hostname);
    List<ContainerRuntimePojoPersist> findByServerNameAndHostname(String serverName, String hostname);
    List<ContainerRuntimePojoPersist> findByProjectUUID(String projectUUID);
    List<ContainerRuntimePojoPersist> findByContainerId(String continuerID);

    ContainerRuntimePojoPersist findByServerNameAndContainerIdAndHostname(String serverName, String containerId,String hostname);



}
