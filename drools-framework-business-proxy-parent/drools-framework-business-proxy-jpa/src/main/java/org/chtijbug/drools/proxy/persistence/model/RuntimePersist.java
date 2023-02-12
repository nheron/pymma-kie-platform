package org.chtijbug.drools.proxy.persistence.model;



import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"serverName"}),
        @UniqueConstraint(columnNames={"hostname"})
})
public class RuntimePersist {
    public enum STATUS {
        UP,
        DOWN
    }
    @javax.persistence.Id
    @GeneratedValue
    private Long uniqueId;

    private String serverName;

    private String version;

    private String branch;


    private String hostname;

    private String serverUrl;

    private String serverPort;

    private String sftpHost;

    private String sftpPort;

    private String status;

    private Date creationDate;

    private Date timeStamp;


    public RuntimePersist(String serverName, String version, String hostname,String serverPort,String sftpPort,String sftpHost,String status) {
        this.serverName = serverName;
        this.version = version;
        this.hostname = hostname;
        this.serverPort = serverPort;
        this.sftpPort = sftpPort;
        this.sftpHost = sftpHost;
        this.status=status;
        this.creationDate = new Date();
    }

    public RuntimePersist(String serverName) {
        this.serverName = serverName;
    }

    public RuntimePersist() {
    }

    public RuntimePersist duplicate(){
        RuntimePersist duplicate = new RuntimePersist(serverName,version,hostname,serverPort,sftpPort,sftpHost,status);
        duplicate.setServerUrl(this.serverUrl);
        return duplicate;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getSftpPort() {
        return sftpPort;
    }

    public void setSftpPort(String sftpPort) {
        this.sftpPort = sftpPort;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getSftpHost() {
        return sftpHost;
    }

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
