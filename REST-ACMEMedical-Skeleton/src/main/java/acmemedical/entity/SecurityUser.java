/********************************************************************************************************
 * File:  SecurityUser.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import acmemedical.rest.serializer.SecurityRoleSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@SuppressWarnings("unused")

/**
 * User class used for (JSR-375) Jakarta EE Security authorization/authentication
 */

//TODO SU01 - Make this into JPA entity and add all the necessary annotations inside the class.
@Entity // Make this class a JPA entity
@Table(name = "security_user") // Optional: name of the table
@NamedQuery(name = "SecurityUser.userByName", query = "SELECT su FROM SecurityUser su WHERE su.username = :param1")
@NamedQuery(name = "SecurityUser.userByPhysicianId", query = "SELECT su FROM SecurityUser su WHERE su.physician.id = :param1")

public class SecurityUser implements Serializable, Principal {
    /** Explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    //TODO SU02 - Add annotations.
    @Id // Primary key annotation
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key values
    @Column(name = "user_id") // Specify the column name
    protected int id;
    
    //TODO SU03 - Add annotations.
    @Column(name = "username", nullable = false, unique = true) // Column details for username
    protected String username;
    
    //TODO SU04 - Add annotations.
    @Column(name = "password_hash", nullable = false) // Column for password hash
    protected String pwHash;
    
    //TODO SU05 - Add annotations.
    @OneToOne(fetch = FetchType.LAZY) // One-to-one relationship with Physician
    @JoinColumn(name = "physician_id", referencedColumnName = "id") // Join column details
    protected Physician physician;
    
    @JoinTable(
    		name = "user_has_role",
    		joinColumns = @JoinColumn(
    				name = "user_id",
    				referencedColumnName = "user_id"),
    		inverseJoinColumns = @JoinColumn(
    				name = "role_id",
    				referencedColumnName = "role_id")
    		)
    @OneToMany
    protected Set<SecurityRole> roles = new HashSet<SecurityRole>();

    public SecurityUser() {
        super();
    }


    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwHash() {
        return pwHash;
    }
    
    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }

    // TODO SU07 - Setup custom JSON serializer
    public Set<SecurityRole> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<SecurityRole> roles) {
        this.roles = roles;
    }

    public Physician getPhysician() {
        return physician;
    }
    
    public void setPhysician(Physician physician) {
        this.physician = physician;
    }

    // Principal
    @Override
    public String getName() {
        return getUsername();
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
        if (obj instanceof SecurityUser otherSecurityUser) {
            // See comment (above) in hashCode():  Compare using only member variables that are
            // truly part of an object's identity
            return Objects.equals(this.getId(), otherSecurityUser.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SecurityUser [id = ").append(id).append(", username = ").append(username).append("]");
        return builder.toString();
    }
    
}