package org.chtijbug.drools.proxy.persistence.model;



import javax.persistence.*;

@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"name"})
})
public class Customer {

    @javax.persistence.Id
    @GeneratedValue
    private Long uniqueId;


    private String name;

    @ManyToMany
    private KieWorkbench kieWorkbench;

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KieWorkbench getKieWorkbench() {
        return kieWorkbench;
    }

    public void setKieWorkbench(KieWorkbench kieWorkbench) {
        this.kieWorkbench = kieWorkbench;
    }
}
