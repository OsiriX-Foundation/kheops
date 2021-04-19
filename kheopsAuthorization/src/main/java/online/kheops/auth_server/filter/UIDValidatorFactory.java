package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.UIDValidator;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.annotation.Priority;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static online.kheops.auth_server.util.Consts.UID_VALIDATOR_PRIORITY;

@Provider
public class UIDValidatorFactory implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        Annotation[][] parameterAnnotations = resourceInfo.getResourceMethod().getParameterAnnotations();
        ArrayList<String> uids = new ArrayList<>();

        for(Annotation[] annotations : parameterAnnotations) {
            for (Annotation annotation1 : annotations) {
                if (annotation1 instanceof UIDValidator) {
                    for (Annotation annotation2 : annotations) {
                        if (annotation2 instanceof PathParam) {
                            uids.add(((PathParam)annotation2).value());
                            break;
                        }
                    }

                }
            }
        }
        if (!uids.isEmpty()) {
            featureContext.register(new CheckUID(uids));
        }
    }

    @Priority(UID_VALIDATOR_PRIORITY)
    private static class CheckUID implements ContainerRequestFilter {
        private final ArrayList<String> types;

        CheckUID(ArrayList<String> types) {
            this.types = types;
        }

        @Override
        public void filter(ContainerRequestContext requestContext) {

            final MultivaluedMap<String, String> pathParam = requestContext.getUriInfo().getPathParameters();

            for(String type : types) {
                if (!pathParam.containsKey(type)) {
                    throw new IllegalStateException("Key: " + type + " not present in PathParameters");
                }

                final String uid = pathParam.get(type).get(0);

                if (!checkValidUID(uid)) {
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(type + " is not a valid UID.").build());
                }
            }
        }

        private boolean checkValidUID(String uid) {
            try {
                new Oid(uid);
                return true;
            } catch (GSSException exception) {
                return false;
            }
        }
    }
}
