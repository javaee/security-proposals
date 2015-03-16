/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme.ee8;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.security.auth.SecurityContext;

@Singleton
public class MyFutureCdiBean {

    @Inject
    private SecurityContext securityContext;

    public String sayHello() {
        if (securityContext.isUserInRole("admin")) {
            return "Hello World!";
        }

        throw new SecurityException("User is unauthorized.");
    }

}
