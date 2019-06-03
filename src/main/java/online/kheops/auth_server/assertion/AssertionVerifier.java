package online.kheops.auth_server.assertion;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AssertionVerifier {

    private static final Logger LOG = Logger.getLogger(AssertionVerifier.class.getName());

    private enum TokenType {
        JWT_BEARER {
            AssertionBuilder getAssertionBuilder(ServletContext servletContext) {
                return new JWTAssertionBuilder(servletContext);
            }
        },
        CAPABILITY {
            AssertionBuilder getAssertionBuilder(ServletContext servletContext) {
                return new CapabilityAssertionBuilder();
            }
        },
        VIEWER {
            AssertionBuilder getAssertionBuilder(ServletContext servletContext) {
                return new ViewerAssertionBuilder(servletContext);
            }
        };

        abstract AssertionBuilder getAssertionBuilder(ServletContext servletContext);
    }

    private AssertionVerifier() {}

    public static Assertion createAssertion(ServletContext servletContext, String accessToken) throws BadAssertionException {
        List<BadAssertionException> exceptionList = new ArrayList<>(3);
        try {
            return TokenType.JWT_BEARER.getAssertionBuilder(servletContext).build(accessToken);
        } catch (BadAssertionException e) {
            exceptionList.add(e);
        }
        try {
            return TokenType.CAPABILITY.getAssertionBuilder(servletContext).build(accessToken);
        } catch (BadAssertionException e) {
            exceptionList.add(e);
        }
        try {
            return TokenType.VIEWER.getAssertionBuilder(servletContext).build(accessToken);
        } catch (BadAssertionException e) {
            exceptionList.add(e);
        }

        final StringBuilder messageBuilder = new StringBuilder("Unable to verify assertion because");
        exceptionList.forEach(e -> messageBuilder.append(", ").append(e.getMessage()));

        final BadAssertionException badAssertionException = new BadAssertionException(messageBuilder.toString());
        exceptionList.forEach(badAssertionException::addSuppressed);

        throw badAssertionException;
    }

}

