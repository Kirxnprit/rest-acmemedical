package acmemedical.rest.resource;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
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
import static acmemedical.utility.MyConstants. MEDICAL_CERTIFICATE_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.MedicalCertificate;

@Path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalCertificateResource {
	
	private static final Logger LOG = LogManager.getLogger();
    private static final String GET_ALL_MEDICAL_CERTIFICATE = "MedicalCertificate.findAll";
    @EJB
    protected ACMEMedicalService service;
    @GET
    public Response getMedicalCertificates() {
        List<MedicalCertificate> medicalCertificates = service.getAll(MedicalCertificate.class, GET_ALL_MEDICAL_CERTIFICATE);
        Response response = Response.ok(medicalCertificates).build();
        return response;
    }

    @GET
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicalCertificateById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	MedicalCertificate medicalCertificate = service.getMedicalCertificateById(id);
        if (medicalCertificate == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(medicalCertificate).build();
    }
    
    @POST
    public Response addMedicalCertificate(MedicalCertificate medicalCertificate) {
    	MedicalCertificate result = service.persistMedicalCertificate(medicalCertificate);
        if (result == null) {
            return Response.status(Status.NOT_MODIFIED).build();
        }
        return Response.status(Status.CREATED).entity(medicalCertificate).build();
    }
    
    @PUT
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMembershipCard(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, MedicalCertificate medicalCertificate) {
    	MedicalCertificate result = service.updateMedicalCertificate(medicalCertificate, id);
        if (result == null) {
            return Response.status(Status.NOT_MODIFIED).build();
        }
        return Response.ok(medicalCertificate).build();
    }

    


}