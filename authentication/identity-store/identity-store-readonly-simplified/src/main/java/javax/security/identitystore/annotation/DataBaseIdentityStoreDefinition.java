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
public @interface DataBaseIdentityStoreDefinition {

	String dataSourceLookup() default "java:comp/DefaultDataSource"; // default data source when omitted 
	String callerQuery();
	String groupsQuery();
	String hashAlgorithm() default ""; // default no hash (for now) todo: make enum?
	String hashEncoding() default ""; // default no encoding (for now) todo: make enum?

}