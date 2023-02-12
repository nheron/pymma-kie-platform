package org.chtijbug.drools.proxy.persistence.model;


import org.chtijbug.drools.console.dto.VariableDefinition;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"templateName"})
})
public class GuidedRulesTemplateDefinition {

    @javax.persistence.Id
    @GeneratedValue
    private Long uniqueId;


    private String templateName;

    @ManyToMany
    private UserGroups projectGroup;

    @ElementCollection
    private List<VariableDefinition> variables = new ArrayList<>();

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
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
