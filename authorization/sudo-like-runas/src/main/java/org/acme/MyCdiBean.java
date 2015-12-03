/*
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

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.security.auth.RunAs;
import java.io.File;
import java.util.stream.Stream;

/**
 * This form of switching user-context should be possible
 * assuming the RolesAllowed is checked before RunAs
 */
public class MyCdiBean {

    @Inject
    private LogsBean logsBean;

    @RolesAllowed("superuser")
    @RunAs("root")
    public void deleteLogs(final File... files) {

        Stream.of(files).forEach(logsBean::delete);
    }

    @RolesAllowed("user")
    @RunAs("root")
    public void addLog(final File... files) {

        Stream.of(files).forEach(logsBean::add);
    }
}
