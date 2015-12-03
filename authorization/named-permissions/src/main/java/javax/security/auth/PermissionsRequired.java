package javax.security.auth;

import java.lang.annotation.*;


/**
 *
 */


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PermissionsRequired {
    String[] value();
}