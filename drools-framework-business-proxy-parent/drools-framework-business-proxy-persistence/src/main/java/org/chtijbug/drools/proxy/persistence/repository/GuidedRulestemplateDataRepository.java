package org.chtijbug.drools.proxy.persistence.repository;

import org.chtijbug.drools.proxy.persistence.model.GuidedRulesTemplateData;
import org.chtijbug.drools.proxy.persistence.model.GuidedRulesTemplateDefinition;
import org.chtijbug.drools.proxy.persistence.model.KieWorkbench;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuidedRulestemplateDataRepository extends MongoRepository<GuidedRulesTemplateData, String> {



    GuidedRulesTemplateData findByGuidedRulesTemplateDefinitionAndLineID(GuidedRulesTemplateDefinition guidedRulesTemplateDefinition,String lineID);
    List<GuidedRulesTemplateData> findByGuidedRulesTemplateDefinition(GuidedRulesTemplateDefinition guidedRulesTemplateDefinition);
}
