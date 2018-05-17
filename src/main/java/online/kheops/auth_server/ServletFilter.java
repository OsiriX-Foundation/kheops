package online.kheops.auth_server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

public class ServletFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("filter was inited");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("filter was called");

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
                        return new Vector<String>(Arrays.asList(newContentType)).elements();
                    } else {
                        return super.getHeaders(name);
                    }
                }

            };

        }


        chain.doFilter(finalRequest, response);//sends request to next resource
    }

    @Override
    public void destroy() {
        System.out.println("filter was destroyed");
    }
}
