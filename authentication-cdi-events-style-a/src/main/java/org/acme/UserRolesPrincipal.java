package org.acme;

import java.security.Principal;
import java.util.List;

public class UserRolesPrincipal implements Principal {
    private final String name;
    private final List<String> roles;

    public UserRolesPrincipal(final String name, final List<String> roles) {

        this.name = name;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }
}
