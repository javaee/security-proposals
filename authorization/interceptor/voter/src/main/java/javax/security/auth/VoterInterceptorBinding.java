package javax.security.auth;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 *
 */
@Inherited
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VoterInterceptorBinding {
}
