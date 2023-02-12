package org.chtijbug.drools.proxy.persistence.model;


import org.chtijbug.drools.console.dto.VariableData;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"lineID"})
})
public class GuidedRulesTemplateData {

    @javax.persistence.Id
    @GeneratedValue
    private Long uniqueId;


    @ManyToMany
    GuidedRulesTemplateDefinition guidedRulesTemplateDefinition;


    private String lineID;
    @ElementCollection
    private List<VariableData> rows = new ArrayList<>();

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public GuidedRulesTemplateDefinition getGuidedRulesTemplateDefinition() {
        return guidedRulesTemplateDefinition;
    }

    public void setGuidedRulesTemplateDefinition(GuidedRulesTemplateDefinition guidedRulesTemplateDefinition) {
        this.guidedRulesTemplateDefinition = guidedRulesTemplateDefinition;
    }

    public String getLineID() {
        return lineID;
    }

    public void setLineID(String lineID) {
        this.lineID = lineID;
    }

    public List<VariableData> getRows() {
        return rows;
    }

    public void setRows(List<VariableData> rows) {
        this.rows = rows;
    }
}
