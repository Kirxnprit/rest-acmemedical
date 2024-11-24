package acmemedical.rest.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Patient;
import acmemedical.utility.MyConstants;
@Path(MyConstants.PATIENT_RESOURCE_NAME)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientResource implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(PatientResource.class);

    @EJB
    protected ACMEMedicalService service;

    @GET
    @RolesAllowed({USER_ROLE, ADMIN_ROLE})
    public List<Patient> getAllPatients() {
        List<Patient> results = new ArrayList<>();

        results = service.getAll(Patient.class, Patient.ALL_PATIENTS_QUERY);

        return results;
    }

    @GET
    @RolesAllowed({USER_ROLE, ADMIN_ROLE})
    @Path(MyConstants.RESOURCE_PATH_ID_PATH)
    public Response getPatientById(@PathParam(MyConstants.RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("****** PatientResource.getPatientById with id " + id);
        System.out.println("****** PatientResource.getPatientById with id " + id);
        Patient result = null;

        result = service.getPatientById(id);
        return Response.status(result == null ? Status.NOT_FOUND : Status.OK).entity(result).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPatient(Patient newPatient) {

        LOG.debug("PatientResource about to persist Patient " + newPatient);
        Patient newPatientWithIdTimestamps = null;
        try {
            newPatientWithIdTimestamps = service.persistPatient(newPatient);
        } catch (Exception e) {
            LOG.debug("caught exception persisting patient", e);
            e.printStackTrace();
        }
        return Response.status(newPatientWithIdTimestamps == null ? Status.NOT_MODIFIED : Status.OK).entity(newPatientWithIdTimestamps).build();
    }

    @PUT
    @Path(MyConstants.RESOURCE_PATH_ID_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response updatePatient(@PathParam(MyConstants.RESOURCE_PATH_ID_ELEMENT) int id,
                                  Patient updatedPatient) {
        Response result = null;
        Patient updatedPatientWithTimestamps =
                service.updatePatientById(id, updatedPatient);
        if (updatedPatientWithTimestamps == null) {
            LOG.debug("updatedPatientWithTimestamps is null");
        } else {
            LOG.debug("updated patient: " + updatedPatient);
        }

        return Response.ok(updatedPatientWithTimestamps).build();
    }

    @DELETE
    @Path(MyConstants.RESOURCE_PATH_ID_PATH)
    @RolesAllowed({ADMIN_ROLE})
    public Response deletePatient(@PathParam("id") int id) {
        LOG.debug("about to delete patient with id " + id);
        Patient patient = service.getPatientById(id);
        LOG.debug("found patient to delete");
        if (patient == null) {
            LOG.debug("patient to delete is null");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        LOG.debug("about to delete patient");
        service.deletePatientById(id); // Method to delete the patient
        return Response.noContent().build();
    }
}
