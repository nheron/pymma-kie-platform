package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.RuntimePersist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuntimeRepository extends JpaRepository<RuntimePersist, Long> {

    public List<RuntimePersist> findByServerName(String serverName);
    public List<RuntimePersist> findByServerNameAndHostname(String serverName,String hostname);
}
