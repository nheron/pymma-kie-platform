package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.GuidedRulesTemplateData;
import org.chtijbug.drools.proxy.persistence.model.GuidedRulesTemplateDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuidedRulestemplateDataRepository extends JpaRepository<GuidedRulesTemplateData, String> {



    GuidedRulesTemplateData findByGuidedRulesTemplateDefinitionAndLineID(GuidedRulesTemplateDefinition guidedRulesTemplateDefinition,String lineID);
    List<GuidedRulesTemplateData> findByGuidedRulesTemplateDefinition(GuidedRulesTemplateDefinition guidedRulesTemplateDefinition);
}
