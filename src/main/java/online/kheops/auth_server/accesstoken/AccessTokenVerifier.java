package online.kheops.auth_server.accesstoken;

import javax.servlet.ServletContext;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public abstract class AccessTokenVerifier {

    private static final List<Class<?>> accessTokenBuilders = getAccessTokenBuilders();

    private static List<Class<?>> getAccessTokenBuilders() {
        List<Class<?>> accessTokenBuilders = new ArrayList<>();
        accessTokenBuilders.add(JWTAccessTokenBuilder.class);
        accessTokenBuilders.add(CapabilityAccessToken.CapabilityAccessTokenBuilder.class);
        accessTokenBuilders.add(ViewerAccessTokenBuilder.class);

        return accessTokenBuilders;
    }

    private AccessTokenVerifier() {}

    public static AccessToken authenticateAccessToken(ServletContext servletContext, String accessToken)
            throws AccessTokenVerificationException {

        List<AccessTokenVerificationException> exceptionList = new ArrayList<>(3);

        for (Class<?> builderClass: accessTokenBuilders) {
            final AccessTokenBuilder accessTokenBuilder;
            Constructor<?> servletContextConstructor;
            try {
                servletContextConstructor = builderClass.getDeclaredConstructor((ServletContext.class));
            } catch (NoSuchMethodException e) {
                servletContextConstructor = null;
            }

            try {
                if (servletContextConstructor != null) {
                    accessTokenBuilder = (AccessTokenBuilder) servletContextConstructor.newInstance(servletContext);
                } else {
                    accessTokenBuilder = (AccessTokenBuilder) builderClass.getDeclaredConstructor().newInstance();
                }
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new IllegalStateException(e);
            }

            try {
                return accessTokenBuilder.build(accessToken);
            } catch (AccessTokenVerificationException e) {
                exceptionList.add(e);
            }
        }

        final StringBuilder messageBuilder = new StringBuilder("Unable to verify accesstoken because");
        exceptionList.forEach(e -> messageBuilder.append(", ").append(e.getMessage()));

        final AccessTokenVerificationException accessTokenVerificationException = new AccessTokenVerificationException(messageBuilder.toString());
        exceptionList.forEach(accessTokenVerificationException::addSuppressed);

        throw accessTokenVerificationException;
    }
}
