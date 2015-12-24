package javax.security.authentication.mechanism.http;

import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpMessageContext {

    /**
     * Checks if the current request is to a protected resource or not. A protected resource
     * is a resource (e.g. a Servlet, JSF page, JSP page etc) for which a constraint has been defined
     * in e.g. <code>web.xml<code>.
     * 
     * @return true if a protected resource was requested, false if a public resource was requested.
     */
    boolean isProtected();

    boolean isAuthenticationRequest();

    /**
     * Asks the container to register the given username and roles in order to make
     * them available to the application for use with {@link HttpServletRequest#isUserInRole(String)} etc.
     * <p>
     * This will also ask the runtime to register an authentication session that will live as long as the
     * HTTP session is valid. 
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should not errors occur) after the {@link ServerAuthContext} or {@link ServerAuthModule}
     * in which this call takes place return control back to the runtime.
     * 
     * @param username the user name that will become the caller principal
     * @param roles the roles associated with the caller principal
     */
    void registerWithContainer(String username, List<String> roles);

    /**
     * Asks the container to register the given username and roles in order to make
     * them available to the application for use with {@link HttpServletRequest#isUserInRole(String)} etc.
     * <p>
     * This will optionally (on the basis of the registerSession parameter) ask the runtime to register an 
     * authentication session that will live as long as the HTTP session is valid. 
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should not errors occur) after the {@link ServerAuthContext} or {@link ServerAuthModule}
     * in which this call takes place return control back to the runtime.
     * 
     * @param username the user name that will become the caller principal
     * @param roles the roles associated with the caller principal
     * @param registerSession if true asks the container to register an authentication setting, if false does not ask this.
     */
    void registerWithContainer(String username, List<String> roles, boolean registerSession);

    /**
     * Checks if during the current request code has asked the runtime to register an authentication session.
     * 
     * @return true if code has asked to register an authentication session, false otherwise.
     */
    boolean isRegisterSession();

    /**
     * Asks the runtime to register an authentication session. This will automatically remember the logged-in status
     * as long as the current HTTP session remains valid. Without this being asked, a SAM has to manually re-authenticate
     * with the runtime at the start of each request.
     * <p>
     * Note that the user name and roles being asked is an implementation detail; there is no portable way to have
     * an auth context read back the user name and roles that were processed by the {@link CallbackHandler}.
     * 
     * @param username the user name for which authentication should be be remembered
     * @param roles the roles for which authentication should be remembered.
     */
    void setRegisterSession(String username, List<String> roles);

    void cleanClientSubject();

    /**
     * Returns the parameters that were provided with the SecurityContect#authenticate(AuthParameters) call.
     *  
     * @return the parameters that were provided with the SecurityContect#authenticate(AuthParameters) call, or a default instance. Never null.
     */
    AuthenticationParameters getAuthParameters();

    /**
     * Returns the handler that the runtime provided to auth context.
     * 
     * @return the handler that the runtime provided to auth context.
     */
    CallbackHandler getHandler();

    /**
     * Returns the module options that were set on the auth module to which this context belongs.
     * 
     * @return the module options that were set on the auth module to which this context belongs.
     */
    Map<String, String> getModuleOptions();

    /**
     * Returns the named module option that was set on the auth module to which this context belongs.
     * 
     * @return the named module option that was set on the auth module to which this context belongs, or null if no option with that name was set.
     */
    String getModuleOption(String key);

    /**
     * Returns the message info instance for the current request.
     * 
     * @return the message info instance for the current request.
     */
    MessageInfo getMessageInfo();

    /**
     * Returns the subject for which authentication is to take place.
     * 
     * @return the subject for which authentication is to take place.
     */
    Subject getClientSubject();

    /**
     * Returns the request object associated with the current request.
     * 
     * @return the request object associated with the current request.
     */
    HttpServletRequest getRequest();

    /**
     * Returns the response object associated with the current request.
     * 
     * @return the response object associated with the current request.
     */
    HttpServletResponse getResponse();

    /**
     * Sets the response status to 401 (not found).
     * <p>
     * As a convenience this method returns SEND_FAILURE, so this method can be used in
     * one fluent return statement from an auth module.
     * 
     * @return {@link AuthStatus#SEND_FAILURE}
     */
    AuthStatus responseUnAuthorized();

    /**
     * Sets the response status to 404 (not found).
     * <p>
     * As a convenience this method returns SEND_FAILURE, so this method can be used in
     * one fluent return statement from an auth module.
     * 
     * @return {@link AuthStatus#SEND_FAILURE}
     */
    AuthStatus responseNotFound();

    /**
     * Asks the container to register the given username and roles in order to make
     * them available to the application for use with {@link HttpServletRequest#isUserInRole(String)} etc.
     *
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should not errors occur) after the {@link ServerAuthContext} or {@link ServerAuthModule}
     * in which this call takes place return control back to the runtime.
     * 
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an auth module.
     * 
     * @param username the user name that will become the caller principal
     * @param roles the roles associated with the caller principal
     * @return {@link AuthStatus#SUCCESS}
     *
     */
    AuthStatus notifyContainerAboutLogin(String username, List<String> roles);

    /**
     * Instructs the container to "do nothing".
     * 
     * <p>
     * This is a somewhat peculiar requirement of JASPIC, which incidentally almost no containers actually require
     * or enforce. 
     * 
     * <p>
     * When intending to do nothing, most JASPIC auth modules simply return "SUCCESS", but according to
     * the JASPIC spec the handler MUST have been used when returning that status. Because of this JASPIC
     * implicitly defines a "protocol" that must be followed in this case; 
     * invoking the CallerPrincipalCallback handler with a null as the username.
     * 
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an auth module.
     * 
     * @return {@link AuthStatus#SUCCESS}
     */
    AuthStatus doNothing();

}