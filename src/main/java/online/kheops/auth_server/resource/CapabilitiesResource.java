package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.CapabilitySecured;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Path("/users")
public class CapabilitiesResource {

    static class CapabilityResponse {
        @XmlElement(name = "secret")
        String secret;
        @XmlElement(name = "description")
        String description;
        @XmlElement(name = "expiration")
        String expiration;
        @XmlElement(name = "revoked")
        boolean revoked;
    }

    @POST
    @Secured
    @CapabilitySecured
    @FormURLEncodedContentType
    @Path("/{user}/capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCapability(@PathParam("user") String username, @FormParam("description") String description, @FormParam("expiration") String expirationDate,
                                     @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        CapabilityResponse capabilityResponse;

        if (description == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No description set").build();
        }

        // parse the given expiration date
        LocalDateTime expirationDateTime;
        if (expirationDate != null) {
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(expirationDate);
                expirationDateTime = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid expiration date").build();
            }
        } else {
            expirationDateTime = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = User.findByUsername(username, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            if (callingUserPk != targetUser.getPk()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Can't set capabilities for a different user").build();
            }

            Capability capability = new Capability();
            capability.setExpiration(expirationDateTime);
            capability.setDescription(description);
            capability.setUser(targetUser);
            targetUser.getCapabilities().add(capability);

            em.persist(capability);
            em.persist(targetUser);

            capabilityResponse = new CapabilityResponse();
            capabilityResponse.secret = capability.getSecret();
            capabilityResponse.description = capability.getDescription();
            capabilityResponse.expiration = ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).toString();
            capabilityResponse.revoked = capability.isRevoked();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.status(Response.Status.CREATED).entity(capabilityResponse).build();
    }

    @POST
    @Secured
    @CapabilitySecured
    @Path("/{user}/capabilities/{secret}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeCapability(@PathParam("user") String username, @PathParam("secret") String secret,
                                     @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        CapabilityResponse capabilityResponse;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = User.findByUsername(username, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            if (callingUserPk != targetUser.getPk()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Can't revoke a different user's capability").build();
            }

            TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :targetUser = c.user AND :secret = c.secret", Capability.class);
            query.setParameter("targetUser", targetUser);
            query.setParameter("secret", secret);
            Capability capability;
            try {
                capability = query.getSingleResult();
            } catch (NoResultException e) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown capability").build();
            }

            capability.setRevoked(true);
            em.persist(capability);

            capabilityResponse = new CapabilityResponse();
            capabilityResponse.secret = capability.getSecret();
            capabilityResponse.description = capability.getDescription();
            capabilityResponse.expiration = ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).toString();
            capabilityResponse.revoked = capability.isRevoked();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.status(Response.Status.OK).entity(capabilityResponse).build();
    }

    @GET
    @Secured
    @CapabilitySecured
    @Path("/{user}/capabilities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCapability(@PathParam("user") String username,
                                     @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        List<CapabilityResponse> capabilityResponses = new ArrayList<>();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = User.findByUsername(username, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            if (callingUserPk != targetUser.getPk()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Can't get capabilities for a different user").build();
            }

            TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :targetUser = c.user", Capability.class);
            query.setParameter("targetUser", targetUser);
            List<Capability> capabilities = query.getResultList();

            for (Capability capability: capabilities) {
                CapabilityResponse capabilityResponse = new CapabilityResponse();
                capabilityResponse.secret = capability.getSecret();
                capabilityResponse.description = capability.getDescription();
                capabilityResponse.expiration = ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).toString();
                capabilityResponse.revoked = capability.isRevoked();

                capabilityResponses.add(capabilityResponse);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        GenericEntity<List<CapabilityResponse>> genericCapabilityResponsesList = new GenericEntity<List<CapabilityResponse>>(capabilityResponses) {};
        return Response.status(Response.Status.OK).entity(genericCapabilityResponsesList).build();
    }
}
