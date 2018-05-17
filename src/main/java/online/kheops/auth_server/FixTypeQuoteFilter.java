package online.kheops.auth_server;

//import online.kheops.auth_server.annotation.FixTypeQuote;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

//@FixTypeQuote
//@Provider
//@PreMatching
////@Priority(Priorities.AUTHENTICATION)
//public class FixTypeQuoteFilter implements ContainerRequestFilter {
//    @Override
//    public void filter(ContainerRequestContext requestContext) {
//        String contentType = requestContext.getHeaderString("Content-Type");
//        if (contentType.equals("")) {
//
//        }
//
//    }
//}
