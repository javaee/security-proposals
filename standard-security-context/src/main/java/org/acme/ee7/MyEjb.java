/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme.ee7;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;

@Singleton
public class MyEjb {

    @Resource
    private SessionContext sessionContext;

    public String sayHello() {

        if (sessionContext.isCallerInRole("admin")) {
            return "Hello World!";
        }

        throw new SecurityException("User is unauthorized.");
    }
}
