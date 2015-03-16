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

import javax.annotation.Resource;
import javax.security.auth.Authenticator;
import javax.security.auth.user.LdapUserSourceDefinition;
import javax.security.auth.user.UserService;

@Authenticator(userSourceName = "java:app/prodUserSource", roleMapperName = "java:app/OneToOneRoleMapper")

@LdapUserSourceDefinition(name = "java:app/prodUserSource", ldapUrl = "ldap://blah", ldapUser = "ElDap", ldapPassword = "#{ALIAS_LDAP}")
public class MyAuthenticator {
    @Resource(lookup = "java:app/prodUserSource")
    private UserService userService;

    private boolean isAccountEnabled(String username) {
        return userService.loadUserByUsername(username).isEnabled();
    }
}