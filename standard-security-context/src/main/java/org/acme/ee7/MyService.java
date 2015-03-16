/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme.ee7;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

public class MyService {

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("/hello")
    public String sayHello(@Context SecurityContext sc) {

        if (sc.isUserInRole("admin")) {
            return "Hello World!";
        }

        throw new SecurityException("User is unauthorized.");
    }
}
