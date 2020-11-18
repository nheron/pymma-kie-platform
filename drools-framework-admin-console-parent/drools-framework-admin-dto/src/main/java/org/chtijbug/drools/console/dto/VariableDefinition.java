package org.chtijbug.drools.console.dto;

public class VariableDefinition {
    private String varName;
    private String dataType;
    private String factType;
    private String factField;
    private String operator;

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getFactType() {
        return factType;
    }

    public void setFactType(String factType) {
        this.factType = factType;
    }

    public String getFactField() {
        return factField;
    }

    public void setFactField(String factField) {
        this.factField = factField;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
