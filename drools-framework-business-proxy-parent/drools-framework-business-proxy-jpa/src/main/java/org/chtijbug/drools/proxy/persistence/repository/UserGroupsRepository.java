package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupsRepository extends JpaRepository<UserGroups, Long> {

    UserGroups findByName(String login);
    UserGroups findByID(String login);

    UserGroups findUserGroupsByProjectPersist(ProjectPersist projectPersist);
    UserGroups findBySpaceName(String spaceName);
    UserGroups findByWorkspaceUserGroupAndProjectName(UserGroups workspaceUserGroups,String projectName);
}
