package org.acme;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;


@Path("account")
public class AccountResource {

    @GET
    @HttpConstraint(
            transportGuarantee = ServletSecurity.TransportGuarantee.NONE,
            rolesAllowed = {"ADMIN", "USER"}
    )
    public List<Account> read() {
        return null;
    }


    @POST
    @HttpConstraint(
            transportGuarantee = ServletSecurity.TransportGuarantee.CONFIDENTIAL,
            rolesAllowed = "ADMIN"
    )
    public void create(final Account account) {

    }

    private class Account { }

}
