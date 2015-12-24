package javax.security.authentication.mechanism.http;

import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpAuthenticationMechanism {

    AuthStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException;
   
    default AuthStatus secureResponse(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {
        return SEND_SUCCESS;
    }
    
    default void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        httpMessageContext.cleanClientSubject();
    }

}
