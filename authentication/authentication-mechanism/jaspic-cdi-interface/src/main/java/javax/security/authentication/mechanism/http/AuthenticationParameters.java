package javax.security.authentication.mechanism.http;

public interface AuthenticationParameters {

    AuthenticationParameters username(String username);

    AuthenticationParameters password(String passWord);

    AuthenticationParameters rememberMe(boolean rememberMe);

    AuthenticationParameters noPassword(boolean noPassword);

    AuthenticationParameters authMethod(String authMethod);

    AuthenticationParameters redirectUrl(String redirectUrl);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    Boolean getRememberMe();

    void setRememberMe(Boolean rememberMe);

    String getAuthMethod();

    void setAuthMethod(String authMethod);

    String getRedirectUrl();

    void setRedirectUrl(String redirectUrl);

    Boolean getNoPassword();

    void setNoPassword(Boolean noPassword);

}