/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme.mysecurityhandler;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;
import javax.security.auth.Secures;

/**
 * Sample implementation of an security binding implementation.
 * This easy sample allows access if you have any of the required roles.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@ApplicationScoped
public class PermissionRequiredAuthorizer {
    // in real projects: @Inject MySecurityCoreImplementation securityImplementation;

    @Secures
    @PermissionRequired
    public boolean checkCustomPermissionLogic(InvocationContext invocationContext) {
        // check annotation on method
        PermissionRequired ann = invocationContext.getMethod().getAnnotation(PermissionRequired.class);

        // check annotation on type
        if (ann == null) {
            ann = invocationContext.getTarget().getClass().getAnnotation(PermissionRequired.class);
        }

        // safe guard: if no annotation is present, this method should not even be called
        if (ann == null) {
            return false;
        }

        final String[] requiredPermissions = ann.value();

        // in real projects: get all permissions for current Principal from securityImplementation.
        // if any of requiredPermissions is contained in the Principals permissions

        return true; // or false if the user doesn't have any of the required permissions.
    }

}
