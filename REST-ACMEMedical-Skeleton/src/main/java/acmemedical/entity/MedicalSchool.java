/********************************************************************************************************
 * File:  MedicalSchool.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * The persistent class for the medical_school database table.
 */
@Entity
@Table(name = "medical_school")
@AttributeOverride(name = "id", column = @Column(name = "school_id")) // MS05 - Attribute override for ID
@Inheritance(strategy = InheritanceType.JOINED) // MS02 - Using JOINED inheritance strategy
@NamedQueries({
    @NamedQuery(
        name = MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME, 
        query = "SELECT m FROM MedicalSchool m LEFT JOIN FETCH m.medicalTrainings"
    ),
    @NamedQuery(
        name = MedicalSchool.IS_DUPLICATE_QUERY_NAME, 
        query = "SELECT COUNT(m) FROM MedicalSchool m WHERE m.name = :name"
    ),
    @NamedQuery(
            name = MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, 
            query = "SELECT DISTINCT m FROM MedicalSchool m LEFT JOIN FETCH m.medicalTrainings WHERE m.id = :param1"
    ),
})
@JsonInclude(JsonInclude.Include.NON_NULL) // MS04 - Include non-null fields in JSON
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PublicSchool.class, name = "public"),
    @JsonSubTypes.Type(value = PrivateSchool.class, name = "private")
})
public abstract class MedicalSchool extends PojoBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ALL_MEDICAL_SCHOOLS_QUERY_NAME = "MedicalSchool.allSchools";
    public static final String IS_DUPLICATE_QUERY_NAME = "MedicalSchool.isDuplicate";
    public static final String SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME = "MedicalSchool.findById";
    
    @Basic(optional = false)
    @Column(name = "name", nullable = false, unique = true, length = 100) // MS05 - Add unique constraint
    private String name;

    @OneToMany(mappedBy = "medicalSchool", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<MedicalTraining> medicalTrainings = new HashSet<>();

    @JsonProperty
    @Transient
    private boolean isPublic; // Transient property for JSON serialization

    @JsonProperty
    @Transient
    protected String type;

    public MedicalSchool() {
        super();
    }

    public MedicalSchool(boolean isPublic) {
        this();
        this.isPublic = isPublic;
    }

    @JsonIgnore
    public Set<MedicalTraining> getMedicalTrainings() {
        return medicalTrainings;
    }

    public void setMedicalTrainings(Set<MedicalTraining> medicalTrainings) {
        this.medicalTrainings = medicalTrainings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Very important: Use getter's for member variables because JPA sometimes needs to intercept those calls
     * and go to the database to retrieve the value
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        // Only include member variables that contribute to identity
        return prime * result + Objects.hash(getId(), getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof MedicalSchool otherMedicalSchool) {
            // Compare using only member variables that are truly part of identity
            return Objects.equals(this.getId(), otherMedicalSchool.getId()) &&
                Objects.equals(this.getName(), otherMedicalSchool.getName());
        }
        return false;
    }
}