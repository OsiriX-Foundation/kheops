package online.kheops.auth_server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
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
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        ServletRequest finalRequest = request;

        // find if the request has missing quotes
        String contentType = httpRequest.getHeader("Content-Type");
        if (contentType.contains("type=application/dicom")){
            String newContentType = contentType.replace("type=application/dicom", "type=\"application/dicom\"");

            finalRequest = new HttpServletRequestWrapper(httpRequest){
                @Override
                public String getHeader(String name) {
                    if (name.equalsIgnoreCase("Content-Type")){
                        return newContentType;
                    } else {
                        return super.getHeader(name);
                    }
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if (name.equalsIgnoreCase("Content-Type")){
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
    public void destroy() { }
}
