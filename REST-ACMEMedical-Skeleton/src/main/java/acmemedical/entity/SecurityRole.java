/********************************************************************************************************
 * File:  SecurityRole.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
@SuppressWarnings("unused")

/**
 * Role class used for (JSR-375) Jakarta EE Security authorization/authentication
 */
@Entity
@Table(name = "security_role")
//TODO SR01 - Make this into JPA entity and add all necessary annotations inside the class.
public class SecurityRole implements Serializable {
    /** Explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-generate primary key
    @Column(name = "role_id")  // Maps to the "id" column in the database
    protected int id;

    @Column(name="name")
    protected String roleName;
    
    @JoinTable(
    		name = "user_has_role",
    	    joinColumns = @JoinColumn(
    	    		name = "role_id",
    	    		referencedColumnName = "role_id"),
    		inverseJoinColumns = @JoinColumn(
    				name = "user_id",
    				referencedColumnName = "user_id")
    		)
    @OneToMany
    protected Set<SecurityUser> users = new HashSet<SecurityUser>();

    public SecurityRole() {
        super();
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Set<SecurityUser> getUsers() {
        return users;
    }

    public void setUsers(Set<SecurityUser> users) {
        this.users = users;
    }

    public void addUserToRole(SecurityUser user) {
        getUsers().add(user);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        // Only include member variables that really contribute to an object's identity
        // i.e. if variables like version/updated/name/etc. change throughout an object's lifecycle,
        // they shouldn't be part of the hashCode calculation
        return prime * result + Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof SecurityRole otherSecurityRole) {
            // See comment (above) in hashCode():  Compare using only member variables that are
            // truly part of an object's identity
            return Objects.equals(this.getId(), otherSecurityRole.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SecurityRole [id = ").append(id).append(", ");
        if (roleName != null)
            builder.append("roleName = ").append(roleName);
        builder.append("]");
        return builder.toString();
    }
}
