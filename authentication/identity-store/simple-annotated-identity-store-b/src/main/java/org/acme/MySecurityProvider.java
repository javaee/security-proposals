/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acme;

import java.security.Principal;

import javax.inject.Inject;
import javax.security.auth.IdentityStore;
import javax.security.auth.OnAuthentication;
import javax.security.auth.OnAuthorization;
import javax.security.auth.UserService;

@IdentityStore
public class MySecurityProvider {

    @Inject
    private UserService userService;

    /**
     * The parameters could suit the credentials mechanism being used.
     */
    @OnAuthentication
    public Principal getPrincipal(String username, String password) {
        // Construct the principal using the user service.
        return null;
    }

    @OnAuthorization
    public String[] getRoles(Principal principal) {
        // Construct an array of roles using the principal and user service. }
        return null;
    }
}
