package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.GuidedRulesTemplateDefinition;
import org.chtijbug.drools.proxy.persistence.model.KieWorkbench;
import org.chtijbug.drools.proxy.persistence.model.RuntimePersist;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuidedRulestemplateDefinitionRepository extends MongoRepository<GuidedRulesTemplateDefinition, String> {

     List<GuidedRulesTemplateDefinition> findByTemplateName(String assetName);

    GuidedRulesTemplateDefinition findByTemplateNameAndProjectGroup(String assetName, UserGroups projectGroup);
}
