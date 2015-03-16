package javax.security.auth;

public @interface Authenticator {
    String userSourceName();

    String roleMapperName();
}
