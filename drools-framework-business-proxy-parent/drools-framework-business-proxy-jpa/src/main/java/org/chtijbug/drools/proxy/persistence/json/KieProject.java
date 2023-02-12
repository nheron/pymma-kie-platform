package org.chtijbug.drools.proxy.persistence.json;

import java.io.Serializable;

public class KieProject implements Serializable {

    private String spaceName;

    private String name;

    public KieProject(){}

    public KieProject(String spaceName, String name) {
        this.spaceName = spaceName;
        this.name = name;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return spaceName+"-"+name;
    }
}
