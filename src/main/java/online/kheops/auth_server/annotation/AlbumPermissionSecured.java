package online.kheops.auth_server.annotation;

import online.kheops.auth_server.filter.AlbumPermissionSecuredContext;
import online.kheops.auth_server.user.AlbumUserPermissions;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Repeatable(AlbumPermissionSecured.List.class)
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface AlbumPermissionSecured {
    AlbumUserPermissions permission();
    AlbumPermissionSecuredContext context();

    @Retention(RUNTIME)
    @Target({TYPE, METHOD})
    @interface  List {
        AlbumPermissionSecured[] value();
    }

}
