package javax.security.identitystore.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface LdapIdentityStoreDefinition {

    String url() default "";
    
    String callerBaseDn() default "";
    String callerNameAttribute() default "uid";
    
	String groupBaseDn() default "";
	String groupNameAttribute() default "cn";
	String groupCallerDnAttribute() default "member";

}