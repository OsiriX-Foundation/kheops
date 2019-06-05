package online.kheops.auth_server.accesstoken;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AccessTokenVerifier {

    private static final Logger LOG = Logger.getLogger(AccessTokenVerifier.class.getName());

    private enum TokenType {
        JWT_BEARER {
            AccessTokenBuilder getAccessTokenBuilder(ServletContext servletContext) {
                return new JWTAccessTokenBuilder(servletContext);
            }
        },
        CAPABILITY {
            AccessTokenBuilder getAccessTokenBuilder(ServletContext servletContext) {
                return new CapabilityAccessTokenBuilder();
            }
        },
        VIEWER {
            AccessTokenBuilder getAccessTokenBuilder(ServletContext servletContext) {
                return new ViewerAccessTokenBuilder(servletContext);
            }
        };

        abstract AccessTokenBuilder getAccessTokenBuilder(ServletContext servletContext);
    }

    private AccessTokenVerifier() {}

    public static AccessToken authenticateAccessToken(ServletContext servletContext, String accessToken) throws BadAccessTokenException {
        List<BadAccessTokenException> exceptionList = new ArrayList<>(3);
        try {
            return TokenType.JWT_BEARER.getAccessTokenBuilder(servletContext).build(accessToken);
        } catch (BadAccessTokenException e) {
            exceptionList.add(e);
        }
        try {
            return TokenType.CAPABILITY.getAccessTokenBuilder(servletContext).build(accessToken);
        } catch (BadAccessTokenException e) {
            exceptionList.add(e);
        }
        try {
            return TokenType.VIEWER.getAccessTokenBuilder(servletContext).build(accessToken);
        } catch (BadAccessTokenException e) {
            exceptionList.add(e);
        }

        final StringBuilder messageBuilder = new StringBuilder("Unable to verify accesstoken because");
        exceptionList.forEach(e -> messageBuilder.append(", ").append(e.getMessage()));

        final BadAccessTokenException badAccessTokenException = new BadAccessTokenException(messageBuilder.toString());
        exceptionList.forEach(badAccessTokenException::addSuppressed);

        throw badAccessTokenException;
    }

}

