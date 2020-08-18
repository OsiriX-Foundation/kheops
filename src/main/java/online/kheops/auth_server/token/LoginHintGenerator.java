package online.kheops.auth_server.token;

import javax.servlet.ServletContext;

public class LoginHintGenerator {

    private final ServletContext context;

    public static LoginHintGenerator createGenerator(final ServletContext servletContext) {
        return new LoginHintGenerator(servletContext);
    }

    private LoginHintGenerator(final ServletContext servletContext) {
        this.context = servletContext;
    }

    public String generate(@SuppressWarnings("SameParameterValue") long expiresIn) {
        return "bogus login hint";
    }
}
