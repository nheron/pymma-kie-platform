package org.chtijbug.drools.console.dto;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class GuidedRuleTemplateDataRow {

    private String lineID;

    private Hashtable dataList = new Hashtable();

    public String getLineID() {
        return lineID;
    }

    public void setLineID(String lineID) {
        this.lineID = lineID;
    }

    public Hashtable getDataList() {
        return dataList;
    }

    public void setDataList(Hashtable dataList) {
        this.dataList = dataList;
    }
}
