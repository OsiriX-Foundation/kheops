package online.kheops.auth_server.filter;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.ViewerTokenAccess;
import online.kheops.auth_server.capability.ScopeType;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static online.kheops.auth_server.util.Consts.*;

//@PreMatching
//@ViewerTokenAccess
//@Provider
//@Priority(VIEWER_TOKEN_ACCESS_PRIORITY)
//public class ViewerTokenFilter implements ContainerRequestFilter {
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) {
//
//        SecuredFilter s = new SecuredFilter();//TODO fait ca pour obtenir le KheopsPrincipal
//        s.filter(requestContext);
//        requestContext = s.requestContext;
//        final MultivaluedMap<String, String> queryParam = requestContext.getUriInfo().getQueryParameters();
//
//        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)requestContext.getSecurityContext().getUserPrincipal());
//        if(requestContext.getSecurityContext().isUserInRole(USER_IN_ROLE.VIEWER_TOKEN)) {
//            UriBuilder builder = requestContext.getUriInfo().getRequestUriBuilder();
//
//            if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
//                if(queryParam.containsKey(INBOX)) {
//                    if(Boolean.valueOf(queryParam.get(INBOX).get(0))) {
//                        requestContext.abortWith(Response.status(BAD_REQUEST).entity("inbox query parameter must be false or null but not true ").build());
//                    }
//                }
//                try {
//                    builder.queryParam(ALBUM, kheopsPrincipal.getAlbumID());
//                } catch (NotAlbumScopeTypeException | AlbumNotFoundException e) {
//                    requestContext.abortWith(Response.status(FORBIDDEN).entity("Album not found").build());
//                }
//                builder.replaceQueryParam(INBOX);
//
//            } else {
//                builder.queryParam(INBOX, "true");
//                builder.replaceQueryParam(ALBUM);
//
//            }
//            requestContext.setRequestUri(builder.build());
//        }
//    }


//}
