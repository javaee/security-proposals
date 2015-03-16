package javax.security.auth;

import java.lang.annotation.ElementType;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, ElementType.METHOD})
public @interface RunAs {
    java.lang.String value();
}