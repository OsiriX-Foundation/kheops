package online.kheops.auth_server.marshaller;

import online.kheops.auth_server.webhook.SignedEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.Key;

import static online.kheops.auth_server.util.HttpHeaders.X_KHEOPS_SIGNATURE;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class WebhookWriter implements MessageBodyWriter<SignedEntity> {

    private final Providers providers;

    public WebhookWriter(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(SignedEntity.class);
    }

    @Override
    public void writeTo(SignedEntity signedEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        Class webhookEntityClass = signedEntity.getEntity().getClass();
        MessageBodyWriter messageBodyWriter = providers.getMessageBodyWriter(webhookEntityClass, webhookEntityClass, annotations, mediaType);

        try(final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            messageBodyWriter.writeTo(signedEntity.getEntity(), webhookEntityClass, webhookEntityClass, annotations, mediaType, httpHeaders, byteArrayOutputStream);
            byteArrayOutputStream.close();

            try {
                final Mac m = Mac.getInstance("HmacSHA256");
                final Key k = new SecretKeySpec(signedEntity.getSecret().getBytes(), "HmacSHA256");
                m.init(k);
                final byte[] b = m.doFinal(byteArrayOutputStream.toByteArray());
                final String hash = bytesToHex(b);
                httpHeaders.add(X_KHEOPS_SIGNATURE, hash);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO faire qqch
            }

            entityStream.write(byteArrayOutputStream.toByteArray());
            entityStream.flush();
        }
    }

    private static String bytesToHex(byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte hash1 : hash) {
            String hex = Integer.toHexString(0xff & hash1);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
