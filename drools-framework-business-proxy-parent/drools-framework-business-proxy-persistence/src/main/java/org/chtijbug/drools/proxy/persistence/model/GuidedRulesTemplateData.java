package org.chtijbug.drools.proxy.persistence.model;


import org.chtijbug.drools.console.dto.VariableData;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class GuidedRulesTemplateData {

    @Id
    @Indexed
    private String ID;

    @DBRef
    @Indexed(unique = false)
    GuidedRulesTemplateDefinition guidedRulesTemplateDefinition;

    @Indexed(unique = false)
    private String lineID;

    private List<VariableData> rows = new ArrayList<>();


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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
