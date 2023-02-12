package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.KieWorkbench;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KieWorkbenchRepository extends JpaRepository<KieWorkbench, Long> {

    KieWorkbench findByName(String name);
    KieWorkbench findByID(String ID);
}
