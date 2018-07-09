package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.FormURLEncodedContentType;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@FormURLEncodedContentType
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class FormURLEncodedContentTypeFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (requestContext.getHeaderString(HttpHeaders.CONTENT_TYPE) == null) {
            requestContext.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        }
    }
}
