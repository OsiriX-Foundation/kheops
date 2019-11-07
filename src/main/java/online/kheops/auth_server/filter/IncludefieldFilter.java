package online.kheops.auth_server.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static online.kheops.auth_server.util.Consts.INCLUDE_FIELD;


@Provider
@PreMatching
public class IncludefieldFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final UriInfo uriInfo = requestContext.getUriInfo();
        if(uriInfo.getQueryParameters().containsKey(INCLUDE_FIELD)) {
            final UriBuilder builder = uriInfo.getRequestUriBuilder();
            final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

            for( Map.Entry<String, List<String>> queryParameter : queryParameters.entrySet() ) {
                final String key = queryParameter.getKey();
                if (key.equals(INCLUDE_FIELD)) {
                    final List<String> values = queryParameter.getValue();

                    builder.replaceQueryParam(key);
                    for (String value : values) {
                        if (value.contains(",")) {
                            String[] lstValues = value.split(",");
                            for (String uniqueValue : lstValues) {
                                builder.queryParam(key, uniqueValue);
                            }
                        } else {
                            builder.queryParam(key, value);
                        }
                    }
                }
            }
            requestContext.setRequestUri( builder.build() );
        }
    }
}
