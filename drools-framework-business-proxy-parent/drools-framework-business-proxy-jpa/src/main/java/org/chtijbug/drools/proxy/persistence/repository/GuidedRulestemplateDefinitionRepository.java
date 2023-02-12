package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.GuidedRulesTemplateDefinition;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuidedRulestemplateDefinitionRepository extends JpaRepository<GuidedRulesTemplateDefinition, String> {

     List<GuidedRulesTemplateDefinition> findByTemplateName(String assetName);

    GuidedRulesTemplateDefinition findByTemplateNameAndProjectGroup(String assetName, UserGroups projectGroup);
}
