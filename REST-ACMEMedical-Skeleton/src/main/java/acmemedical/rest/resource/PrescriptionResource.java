package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.PRESCRIPTION_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.USER_ROLE;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Medicine;
import acmemedical.entity.SecurityUser;
import acmemedical.utility.MyConstants;
import acmemedical.entity.Physician;
import acmemedical.entity.Prescription;

@Path(PRESCRIPTION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrescriptionResource{
	
    @Inject
    protected SecurityContext sc;  
    
    @Inject
    protected ACMEMedicalService acmeMedicalService;  

    @GET
    @RolesAllowed({USER_ROLE, ADMIN_ROLE})
    public Response getPrescriptions() {
        List<Prescription> prescriptions = acmeMedicalService.getAllPrescriptions();
        return Response.status(Status.OK).entity(prescriptions).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path( MyConstants. PHYSICIAN_PATIENT_MEDICINE_RESOURCE_PATH)
    public Response getPrescriptionById(
            @PathParam("physicianId") int physicianId,
            @PathParam("patientId") int patientId
            ) {
        Prescription prescription = acmeMedicalService.getPrescriptionById(physicianId, patientId);
        if (prescription != null) {
            return Response.status(Status.OK).entity(prescription).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    @Transactional
    public Response addPrescription(Prescription prescription) {
        acmeMedicalService.addPrescription(prescription);
        return Response.status(Status.CREATED).entity(prescription).build();
    }


    @DELETE
    @Path(MyConstants.PHYSICIAN_PATIENT_MEDICINE_RESOURCE_PATH)
    @RolesAllowed({ADMIN_ROLE})
    @Transactional
    public Response deletePrescription(
            @PathParam("physicianId") int physicianId,
            @PathParam("patientId") int patientId) {
        boolean success = acmeMedicalService.deletePrescription(physicianId, patientId);
        if (success) {
            return Response.status(Status.NO_CONTENT).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }


    @PUT
    @Path(MyConstants.PHYSICIAN_PATIENT_MEDICINE_RESOURCE_PATH)
    @RolesAllowed({ADMIN_ROLE})
    @Transactional
    public Response updatePrescription(
            @PathParam("physicianId") int physicianId,
            @PathParam("patientId") int patientId,
            Prescription updatedPrescription) {
        boolean success = acmeMedicalService.updatePrescription(physicianId, patientId,updatedPrescription);
        if (success) {
            return Response.status(Status.OK).entity(updatedPrescription).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
 // New method to associate a prescription with a patient
    @POST
    @Path("/associate-prescription/{physicianId}/{patientId}/{medicineId}")
    @RolesAllowed({ADMIN_ROLE})
    @Transactional
    public Response associateMedicine(
            @PathParam("physicianId") int physicianId,
            @PathParam("patientId") int patientId ,
            @PathParam ("prescriptionId") int prescriptionId) {
        boolean success = acmeMedicalService.associateMedicine(physicianId, patientId , prescriptionId);
        if (success) {
            return Response.status(Status.OK).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

}

	