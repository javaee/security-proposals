package javax.security.auth;

import javax.interceptor.InvocationContext;

/**
 *
 */
public interface AccessDecisionVoter {

    // Throws EJBAccessException when voter determines the subject/principal has no access.
    void checkPermission(InvocationContext context);
}
