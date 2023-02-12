package org.chtijbug.drools.proxy.persistence.model;

import org.chtijbug.drools.proxy.persistence.types.JSONBUserType;
import org.hibernate.annotations.TypeDef;


import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"})
)
public class UserRoles {

    @Id
    @GeneratedValue
    private Long uniqueId;

    private String name;

    public UserRoles() {
    }

    public UserRoles( String name) {
        this.name = name;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoles userRoles = (UserRoles) o;
        return Objects.equals(name, userRoles.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
