package acmemedical.rest.resource;
import static acmemedical.utility.MyConstants.CREDENTIAL_RESOURCE_NAME;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import java.security.Principal;

import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.ServletContext;

import jakarta.ws.rs.GET;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.soteria.WrappingCallerPrincipal;

import acmemedical.entity.SecurityUser;

@Path(CREDENTIAL_RESOURCE_NAME)
@Produces(MediaType.APPLICATION_JSON)
public class CredentialResource {

    @Inject
    protected ServletContext servletContext;

    @Inject
    protected SecurityContext securityContent;

    @GET
    public Response getCredentials() {
        servletContext.log("testing credentials ...");
        Response response = null;
        Principal callerPrincipal = securityContent.getCallerPrincipal();
        if (callerPrincipal == null) {
            response = Response.status(UNAUTHORIZED).build();
        }
        else {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal)callerPrincipal;
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            response = Response.ok(sUser).build();
        }
        return response;
    }

}