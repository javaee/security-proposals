package org.acme;

import javax.annotation.security.RolesAllowed;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;


@Path("account")
@ServletSecurity(httpMethodConstraints = {
        @HttpMethodConstraint(value = "POST", rolesAllowed = {"USER"}),
        @HttpMethodConstraint(value = "DEAD", rolesAllowed = {"ADMIN"})
})
public class AccountResource {

    @GET
    @RolesAllowed("USER")
    public List<Account> read() {
        return null;
    }


    @POST
    @HttpConstraint(rolesAllowed = "ADMIN")
    public void create(final Account account) {

    }

    private class Account { }

}
