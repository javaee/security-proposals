package javax.security.auth;

public @interface AuthenticationMechanism {
    String userSourceName();

    String roleMapperName();
}
