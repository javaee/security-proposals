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

import javax.inject.Inject;
import javax.security.auth.AppLoginModule;
import javax.security.auth.PasswordLoginModule;
import javax.security.auth.User;
import javax.security.auth.UserService;
import javax.security.auth.login.FailedLoginException;
import java.util.List;

@AppLoginModule
public class MyAuthenticator implements PasswordLoginModule {

    private User user;

    @Inject
    private UserService userService;

    public MyAuthenticator() {
    }

    public void authenticate(String name, String password) throws FailedLoginException {
        user = userService.getByNameAndPassword(name, password);
        if (user == null) {
            throw new FailedLoginException();
        }
    }

    public String getUserName() {
        return user.getName();
    }

    public List<String> getApplicationRoles() {
        return user.getRoles();
    }
}