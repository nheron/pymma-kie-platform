package org.chtijbug.drools.proxy.persistence.model;



import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(uniqueConstraints ={
        @UniqueConstraint(columnNames={"transactionId"})
})
public class BusinessTransactionPersistence {


    @javax.persistence.Id
    @GeneratedValue
    private Long uniqueId;

    private String transactionId;

    private Integer year;

    private Integer month;

    private Integer day;

    private Integer hour;

    private Integer minute;

    private Integer second;

    private Integer millis;

    private String groupID;

    private String artefactID;

    private String version;

    private String containerId;

    private String serverName;


    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public Integer getMillis() {
        return millis;
    }

    public void setMillis(Integer millis) {
        this.millis = millis;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getArtefactID() {
        return artefactID;
    }

    public void setArtefactID(String artefactID) {
        this.artefactID = artefactID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }


}
