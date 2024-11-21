package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.MEDICINE_SUBRESOURCE_NAME ;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;


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
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Medicine;


@Path(MEDICINE_SUBRESOURCE_NAME )
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicineResource{
	
	private static final Logger LOG = LogManager.getLogger();
    private static final String GET_ALL_MEDICINE = "Medicine.findAll";

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;
	
    @GET
    @RolesAllowed({ADMIN_ROLE})
	public Response getMedicine() {
    	
    	List<Medicine> medicines = service.getAll(Medicine.class, GET_ALL_MEDICINE);
    	Response response = Response.ok(medicines).build();
		return response;
	}
	
    
    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
	public Response getMedicinesById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	
    	Medicine medicine = service.getMedicineById(id);
    	return Response.status(medicine == null ? Status.BAD_REQUEST :
    		Status.OK).entity(medicine).build();
	}
    
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicine(Medicine medicine) {
    	
    	Response response = null;
    	
    	Medicine result = service.persistMedicine(medicine);
    	
    	return Response.status(result == null ? Status.NOT_MODIFIED :
    		Status.OK).entity(medicine).build();
    }
    
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMedicne(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Medicine medicine) {
    	
    	Response response = null;
    	
    	Medicine result = service.updateMedicine(medicine, id);
    	
    	return Response.status(result == null ? Status.NOT_MODIFIED :
    		Status.OK).entity(medicine).build();
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMedicine(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	
    	Response response = null;
    	
    	service.deleteMedicine(id);
    	
    	return Response.status(Status.OK).build();
    }

}
