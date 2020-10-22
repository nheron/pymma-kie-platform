package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupsRepository extends MongoRepository<UserGroups, String> {

    UserGroups findByName(String login);
    UserGroups findByID(String login);

    UserGroups findUserGroupsByProjectPersist(ProjectPersist projectPersist);
}
