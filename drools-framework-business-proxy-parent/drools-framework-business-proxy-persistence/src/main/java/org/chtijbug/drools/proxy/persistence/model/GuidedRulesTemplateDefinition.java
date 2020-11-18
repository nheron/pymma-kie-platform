package org.chtijbug.drools.proxy.persistence.model;


import org.chtijbug.drools.console.dto.VariableDefinition;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class GuidedRulesTemplateDefinition {

    @Id
    @Indexed
    private String ID;

    @Indexed(unique = false)
    private String templateName;

    @DBRef
    private UserGroups projectGroup;

    private List<VariableDefinition> variables = new ArrayList<>();

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }



    public UserGroups getProjectGroup() {
        return projectGroup;
    }

    public void setProjectGroup(UserGroups projectGroup) {
        this.projectGroup = projectGroup;
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableDefinition> variables) {
        this.variables = variables;
    }
}
