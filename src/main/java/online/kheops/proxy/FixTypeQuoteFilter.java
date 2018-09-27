package online.kheops.proxy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.*;

// Some dicomWeb web clients (OsiriX for example) send a improperly formed Content-Type where the type is not in quotes
// For example:
// Content-Type: multipart/related; type=application/dicom; boundary=Boundary-9BB5FED5-7553-4440-A60E-F36BBD8C585F
// Instead of:
// Content-Type: multipart/related; type="application/dicom"; boundary=Boundary-9BB5FED5-7553-4440-A60E-F36BBD8C585F
//
// Jersey notices this and immediately returns a Bad Request. In order to be compatible, we use this filter to fix the content-type.

public class FixTypeQuoteFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) {/* empty */}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        ServletRequest finalRequest = request;

        // find if the request has missing quotes
        final String contentType = httpRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.contains("type=application/dicom")){
            final String newContentType = contentType.replace("type=application/dicom", "type=\"application/dicom\"");

            finalRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getHeader(String name) {
                    if (name.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                        return newContentType;
                    } else {
                        return super.getHeader(name);
                    }
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if (name.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                        return new Vector<>(Collections.singletonList(newContentType)).elements();
                    } else {
                        return super.getHeaders(name);
                    }
                }
            };
        }
        chain.doFilter(finalRequest, response);//sends request to next resource
    }

    @Override
    public void destroy() {/* empty */}
}
