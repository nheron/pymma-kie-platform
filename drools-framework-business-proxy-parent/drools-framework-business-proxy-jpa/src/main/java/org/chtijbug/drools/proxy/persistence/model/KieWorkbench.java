package org.chtijbug.drools.proxy.persistence.model;



import javax.persistence.*;

@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"name"})
})
public class KieWorkbench {

    @Id
    @GeneratedValue
    private Long uniqueId;


    private String name;

    private String internalUrl;

    private String externalUrl;

    private byte[] logo;

    private String gitURL;

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

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getGitURL() {
        return gitURL;
    }

    public void setGitURL(String gitURL) {
        this.gitURL = gitURL;
    }
}
