/********************************************************************************************************
 * File:  ACMEMedicalService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.ejb;

import static acmemedical.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmemedical.utility.MyConstants.PARAM1;
import static acmemedical.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmemedical.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmemedical.utility.MyConstants.PU_NAME;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.entity.Physician.ALL_PHYSICIANS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.IS_DUPLICATE_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Patient;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Medicine;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import acmemedical.entity.SecurityRole;
import acmemedical.entity.SecurityUser;
import acmemedical.entity.Physician;
import acmemedical.entity.MedicalSchool;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMEMedicalService
 */
@Singleton
public class ACMEMedicalService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Physician> getAllPhysicians() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Physician> cq = cb.createQuery(Physician.class);
        cq.select(cq.from(Physician.class));
        return em.createQuery(cq).getResultList();
    }

    public Physician getPhysicianById(int id) {
        return em.find(Physician.class, id);
    }

    @Transactional
    public Physician persistPhysician(Physician newPhysician) {
        em.persist(newPhysician);
        return newPhysician;
    }

    @Transactional
    public void buildUserForNewPhysician(Physician newPhysician) {
        SecurityUser userForNewPhysician = new SecurityUser();
        userForNewPhysician.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPhysician.getFirstName() + "." + newPhysician.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPhysician.setPwHash(pwHash);
        userForNewPhysician.setPhysician(newPhysician);
        //
        TypedQuery<SecurityRole> query = em.createNamedQuery("SecurityRole.userRoleByName", SecurityRole.class); 
        query.setParameter("param1", "USER_ROLE");
        SecurityRole userRole = null; /* TODO ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */ 

        try {
            userRole = query.getSingleResult();
        } catch (Exception e) {
            LOG.error("caught exception attempting to get SecurityUser by name", e);
        }
        //
        userForNewPhysician.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPhysician);
        em.persist(userForNewPhysician);
    }

    @Transactional
    public Medicine setMedicineForPhysicianPatient(int physicianId, int patientId, Medicine newMedicine) {
        Physician physicianToBeUpdated = em.find(Physician.class, physicianId);
        if (physicianToBeUpdated != null) { // Physician exists
            Set<Prescription> prescriptions = physicianToBeUpdated.getPrescriptions();
            prescriptions.forEach(p -> {
                if (p.getPatient().getId() == patientId) {
                    if (p.getMedicine() != null) { // Medicine exists
                        Medicine medicine = em.find(Medicine.class, p.getMedicine().getId());
                        medicine.setMedicine(newMedicine.getDrugName(),
                        				  newMedicine.getManufacturerName(),
                        				  newMedicine.getDosageInformation());
                        em.merge(medicine);
                    }
                    else { // Medicine does not exist
                        p.setMedicine(newMedicine);
                        em.merge(physicianToBeUpdated);
                    }
                }
            });
            return newMedicine;
        }
        else return null;  // Physician doesn't exists
    }

    /**
     * To update a physician
     * 
     * @param id - id of entity to update
     * @param physicianWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Physician updatePhysicianById(int id, Physician physicianWithUpdates) {
    	Physician physicianToBeUpdated = getPhysicianById(id);
        if (physicianToBeUpdated != null) {
            em.refresh(physicianToBeUpdated);
            em.merge(physicianWithUpdates);
            em.flush();
        }
        return physicianToBeUpdated;
    }

    /**
     * To delete a physician by id
     * 
     * @param id - physician id to delete
     */
    @Transactional
    public void deletePhysicianById(int id) {
        Physician physician = getPhysicianById(id);
        if (physician != null) {
        	TypedQuery<SecurityUser> findUser = em.createNamedQuery("SecurityUser.userByPhysicianId", SecurityUser.class);

			findUser.setParameter("param1", id);
			SecurityUser sUser = null;

			try {
				sUser = findUser.getSingleResult();
				em.remove(sUser);
				LOG.debug("just finished removing Physicianuser");
				em.remove(physician);
				LOG.debug("just finished removing physician");
			} catch (Exception e) {
				LOG.debug("***  caught exception attempting to delete physician");
				e.printStackTrace();
			}
		}
	}


    
    public List<MedicalSchool> getAllMedicalSchools() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalSchool> cq = cb.createQuery(MedicalSchool.class);
        cq.select(cq.from(MedicalSchool.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public MedicalSchool getMedicalSchoolById(int id) {
        TypedQuery<MedicalSchool> specificMedicalSchoolQuery = em.createNamedQuery(SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, MedicalSchool.class);
        specificMedicalSchoolQuery.setParameter(PARAM1, id);
        return specificMedicalSchoolQuery.getSingleResult();
    }
    
    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }
    @Transactional
    public <T> T persist(T newEntity) {
        em.persist(newEntity);
        return newEntity;
    }

    @Transactional
    public MedicalSchool deleteMedicalSchool(int id) {
        //MedicalSchool ms = getMedicalSchoolById(id);
    	MedicalSchool ms = getById(MedicalSchool.class, MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, id);
        if (ms != null) {
            Set<MedicalTraining> medicalTrainings = ms.getMedicalTrainings();
            List<MedicalTraining> list = new LinkedList<>();
            medicalTrainings.forEach(list::add);
            list.forEach(mt -> {
                if (mt.getCertificate() != null) {
                    MedicalCertificate mc = getById(MedicalCertificate.class, MedicalCertificate.ID_CTF_QUERY_NAME, mt.getCertificate().getId());
                    mc.setMedicalTraining(null);
                }
                mt.setCertificate(null);
                em.merge(mt);
            });
            em.remove(ms);
            return ms;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    
    public boolean isDuplicated(MedicalSchool newMedicalSchool) {
        TypedQuery<Long> allMedicalSchoolsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allMedicalSchoolsQuery.setParameter(PARAM1, newMedicalSchool.getName());
        return (allMedicalSchoolsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public MedicalSchool persistMedicalSchool(MedicalSchool newMedicalSchool) {
        em.persist(newMedicalSchool);
        return newMedicalSchool;
    }

    @Transactional
    public MedicalSchool updateMedicalSchool(int id, MedicalSchool updatingMedicalSchool) {
    	MedicalSchool medicalSchoolToBeUpdated = getMedicalSchoolById(id);
        if (medicalSchoolToBeUpdated != null) {
            em.refresh(medicalSchoolToBeUpdated);
            medicalSchoolToBeUpdated.setName(updatingMedicalSchool.getName());
            em.merge(medicalSchoolToBeUpdated);
            em.flush();
        }
        return medicalSchoolToBeUpdated;
    }
    
    @Transactional
    public MedicalTraining persistMedicalTraining(MedicalTraining newMedicalTraining) {
        em.persist(newMedicalTraining);
        return newMedicalTraining;
    }
    
    public MedicalTraining getMedicalTrainingById(int mtId) {
        TypedQuery<MedicalTraining> allMedicalTrainingQuery = em.createNamedQuery(MedicalTraining.FIND_BY_ID, MedicalTraining.class);
        allMedicalTrainingQuery.setParameter(PARAM1, mtId);
        return allMedicalTrainingQuery.getSingleResult();
    }

    @Transactional
    public MedicalTraining updateMedicalTraining(int id, MedicalTraining medicalTrainingWithUpdates) {
    	MedicalTraining medicalTrainingToBeUpdated = getMedicalTrainingById(id);
        if (medicalTrainingToBeUpdated != null) {
            em.refresh(medicalTrainingToBeUpdated);
            em.merge(medicalTrainingWithUpdates);
            em.flush();
        }
        return medicalTrainingToBeUpdated;
    }
    
    @Transactional
    public Medicine getMedicineById(int id) {
        Medicine medicine = em.find(Medicine.class, id);
        return medicine;
    }

    @Transactional
    public Medicine getMedicineByDrugAndManufacturer(String drugName, String manufacturerName) {
        Medicine result = null;

        TypedQuery<Medicine> MedicinebyName = em.createNamedQuery(
            "SELECT m FROM Medicine m WHERE m.drugName = :param1 AND m.manufacturerName = :param2",
            Medicine.class
        );
        MedicinebyName.setParameter("param1", drugName);
        MedicinebyName.setParameter("param2", manufacturerName);

        result = MedicinebyName.getSingleResult();
        return result;
    }

    @Transactional
    public Medicine persistMedicine(Medicine medicine) {
        TypedQuery<Long> duplicateMedicineQuery = em.createNamedQuery(
            "SELECT COUNT(m) FROM Medicine m WHERE m.drugName = :param1 AND m.manufacturerName = :param2",
            Long.class
        );

        duplicateMedicineQuery.setParameter("param1", medicine.getDrugName());
        duplicateMedicineQuery.setParameter("param2", medicine.getManufacturerName());

        if (duplicateMedicineQuery.getSingleResult() >= 1) {
            return null;
        } else {
            em.persist(medicine);
            return medicine;
        }
    }

    @Transactional
    public void deleteMedicine(int id) {
        Medicine medicine = em.find(Medicine.class, id);

        try {
            if (medicine != null) {
                em.remove(medicine);
            } else {
                throw new Exception(String.format("Could not find Medicine with ID %d", id));
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Transactional
    public Medicine updateMedicine(Medicine updatedMedicine, int id) {
        Medicine existingMedicine = em.find(Medicine.class, id);

        TypedQuery<Long> duplicateMedicineQuery = em.createQuery(
            "SELECT COUNT(m) FROM Medicine m WHERE m.drugName = :param1 AND m.manufacturerName = :param2",
            Long.class
        );

        duplicateMedicineQuery.setParameter("param1", updatedMedicine.getDrugName());
        duplicateMedicineQuery.setParameter("param2", updatedMedicine.getManufacturerName());

        if (existingMedicine != null && duplicateMedicineQuery.getSingleResult() <= 1) {
            existingMedicine.setDrugName(updatedMedicine.getDrugName());
            existingMedicine.setManufacturerName(updatedMedicine.getManufacturerName());
            existingMedicine.setDosageInformation(updatedMedicine.getDosageInformation());
            existingMedicine.setChemicalName(updatedMedicine.getChemicalName());
            existingMedicine.setGenericName(updatedMedicine.getGenericName());

            em.merge(existingMedicine);
            em.flush();
        }

        return existingMedicine;
    }
    
 // Method to get all prescriptions
    public List<Prescription> getAllPrescriptions() {
        return em.createNamedQuery("Prescription.findAll", Prescription.class).getResultList();
    }

    // Method to get a prescription by physicianId, patientId, and medicineId
    public Prescription getPrescriptionById(int physicianId, int patientId) {
        PrescriptionPK pk = new PrescriptionPK(physicianId, patientId);
        return em.find(Prescription.class, pk);
    }

    // Method to add a new prescription
    public void addPrescription(Prescription prescription) {
        em.persist(prescription);
    }

    // Method to delete a prescription
    public boolean deletePrescription(int physicianId, int patientId) {
        PrescriptionPK pk = new PrescriptionPK(physicianId, patientId);
        Prescription prescription = em.find(Prescription.class, pk);
        if (prescription != null) {
            em.remove(prescription);
            return true;
        }
        return false;
    }

    // Method to update a prescription
    public boolean updatePrescription(int physicianId, int patientId, Prescription updatedPrescription) {
        PrescriptionPK pk = new PrescriptionPK(physicianId, patientId);
        Prescription existingPrescription = em.find(Prescription.class, pk);
        if (existingPrescription != null) {
            existingPrescription.setPatient(updatedPrescription.getPatient());
            existingPrescription.setNumberOfRefills(updatedPrescription.getNumberOfRefills());
            existingPrescription.setPrescriptionInformation(updatedPrescription.getPrescriptionInformation());
            em.merge(existingPrescription);
            return true;
        }
        return false;
    }

    // Method to associate a medicine with a prescription
    public boolean associateMedicine(int physicianId, int patientId, int prescriptionId) {
        PrescriptionPK pk = new PrescriptionPK(physicianId, patientId);
        Prescription prescription = em.find(Prescription.class, pk);
        if (prescription != null) {
                em.merge(prescription);
                return true;
            }
        
        return false;
    }
    @Transactional
    public Patient getPatientById(int id) {
        LOG.debug("********* getPatient with id " + id + "");
        System.out.println("********* getPatient with id " + id + "");
        Patient result = null;
        try {
            LOG.debug("service about to find patient with id " + id);
            result = em.find(Patient.class, id);
            LOG.debug("just found patient: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    public Patient persistPatient(Patient newPatient) {
        LOG.debug("Persisting new patient: " + newPatient);
        em.persist(newPatient);
        return newPatient;
    }

    @Transactional
    public Patient updatePatientById(int id, Patient patientWithUpdates) {
        LOG.debug("********* updatePatient with id " + id + "");
        Patient patientToBeUpdated = getPatientById(id);
        LOG.debug("patientToBeUpdated: " + patientToBeUpdated);
        if (patientToBeUpdated != null) {
            LOG.debug("********* patient to be updated is not null...merging");
            em.refresh(patientToBeUpdated);
            LOG.debug("just refreshed patient to be updated... about to merge");
            em.merge(patientWithUpdates);
            LOG.debug("just finished merging");
            em.flush();
        }
        return patientToBeUpdated;
    }

    @Transactional
    public void deletePatientById(int id) {
        LOG.debug("service about to delete patient");
        Patient patientToBeDeleted = getPatientById(id);
        if (patientToBeDeleted != null) {
            LOG.debug("patient to delete is not null... refreshing");
            em.refresh(patientToBeDeleted);
            LOG.debug("about to remove patient");
            em.remove(patientToBeDeleted);
            LOG.debug("patient successfully removed");
        }
    }
    

    /**
     * Fetches all MedicalCertificate records from the database.
     * 
     * @return a list of all MedicalCertificate records
     */
    @Transactional
    public List<MedicalCertificate> getAllMedicalCertificates() {
        try {
            return em.createNamedQuery(MedicalCertificate.ALL_CTF_QUERY_NAME, MedicalCertificate.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches a MedicalCertificate by its ID.
     * 
     * @param id the ID of the MedicalCertificate to fetch
     * @return the MedicalCertificate entity or null if not found
     */
    @Transactional
    public MedicalCertificate getMedicalCertificateById(int id) {
        try {
            return em.createNamedQuery(MedicalCertificate.ID_CTF_QUERY_NAME, MedicalCertificate.class)
                     .setParameter("param1", id)
                     .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Persists a new MedicalCertificate in the database.
     * 
     * @param newCertificate the MedicalCertificate entity to persist
     * @return the persisted MedicalCertificate or null if the operation fails
     */
    @Transactional
    public MedicalCertificate persistMedicalCertificate(MedicalCertificate newCertificate) {
        try {
            em.persist(newCertificate);
            em.flush(); // Ensures changes are immediately reflected
            return newCertificate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates an existing MedicalCertificate in the database.
     * 
     * @param updatedCertificate the MedicalCertificate with updated data
     * @param id the ID of the MedicalCertificate to update
     * @return the updated MedicalCertificate or null if not found
     */
    @Transactional
    public MedicalCertificate updateMedicalCertificate(MedicalCertificate updatedCertificate, int id) {
        try {
            MedicalCertificate existingCertificate = em.find(MedicalCertificate.class, id);
            if (existingCertificate != null) {
                updatedCertificate.setId(id); // Ensure the same ID is retained
                em.merge(updatedCertificate);
                em.flush();
                return updatedCertificate;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    
    
}