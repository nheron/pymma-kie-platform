package org.chtijbug.drools.console.dto;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class GuidedRuleTemplate {

    private String workbenchName;

    private String spaceName;

    private String projectName;

    private String name;

    private Hashtable definitionList = new Hashtable();

    private List<GuidedRuleTemplateDataRow> rows = new ArrayList<>();

    public String getWorkbenchName() {
        return workbenchName;
    }

    public void setWorkbenchName(String workbenchName) {
        this.workbenchName = workbenchName;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Hashtable getDefinitionList() {
        return definitionList;
    }

    public void setDefinitionList(Hashtable definitionList) {
        this.definitionList = definitionList;
    }

    public List<GuidedRuleTemplateDataRow> getRows() {
        return rows;
    }

    public void setRows(List<GuidedRuleTemplateDataRow> rows) {
        this.rows = rows;
    }
}
