package org.chtijbug.drools.proxy.persistence.model;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"login"})
})
public class User {

    @javax.persistence.Id
    @GeneratedValue
    private Long uniqueId;


    private String login;

    private String wbName;

    private String password;


    public User() {
    }

    public User( String login, String password) {
        this.uniqueId = uniqueId;
        this.login = login;
        this.password = password;
    }

    @OneToMany
    private List<UserRoles> userRoles = new ArrayList<>();

    @OneToMany
    private List<UserGroups> userGroups = new ArrayList<>();

    @ManyToMany
    private Customer customer;

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<UserRoles> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRoles> userRoles) {
        this.userRoles = userRoles;
    }

    public List<UserGroups> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroups> userGroups) {
        this.userGroups = userGroups;
    }

    public String getWbName() {
        return wbName;
    }

    public void setWbName(String wbName) {
        this.wbName = wbName;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
