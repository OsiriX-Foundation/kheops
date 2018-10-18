package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.CacheControlHeader;

import javax.ws.rs.container.*;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
public class CacheFilterFactory implements DynamicFeature {

    private static final CacheResponseFilter NO_CACHE_FILTER =
            new CacheResponseFilter("no-cache");

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        CacheControlHeader cch = resourceInfo.getResourceMethod().getAnnotation(CacheControlHeader.class);
        if (cch == null) {
            featureContext.register(NO_CACHE_FILTER);
        } else {
            featureContext.register(new CacheResponseFilter(cch.value()));
        }
    }

    private static class CacheResponseFilter implements ContainerResponseFilter {
        private final String headerValue;

        CacheResponseFilter(String headerValue) {
            this.headerValue = headerValue;
        }

        @Override
        public void filter(ContainerRequestContext containerRequestContext,
                           ContainerResponseContext containerResponseContext) {
            // attache Cache Control header to each response
            // based on the annotation value
            containerResponseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, headerValue);
        }
    }
}
