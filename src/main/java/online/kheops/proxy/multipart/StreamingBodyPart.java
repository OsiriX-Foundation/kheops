package online.kheops.proxy.multipart;

import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class StreamingBodyPart {
    private Object entity;
    private MediaType mediaType;
    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    private ContentDisposition contentDisposition;

    public StreamingBodyPart(final Object entity, final MediaType mediaType) {
        this.entity = entity;
        this.mediaType = mediaType;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    public ContentDisposition getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(ContentDisposition contentDisposition) {
        this.contentDisposition = contentDisposition;
    }
}
