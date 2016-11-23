/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package javax.security.auth;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Interceptor
@VoterInterceptorBinding
public class VoterInterceptor {

    @AroundInvoke
    public Object interceptMethod(InvocationContext context) throws Exception {

        Voter voterAnnotation = getVoterAnnotation(context);
        if (voterAnnotation != null) {
            executeVoter(voterAnnotation, context);
        }
        return context.proceed();
    }

    private void executeVoter(Voter voterAnnotation, InvocationContext context) {
        AccessDecisionVoter accessDecisionVoter = getContextualReference(voterAnnotation.value());

        if (accessDecisionVoter != null) {
            accessDecisionVoter.checkPermission(context);
        }
    }

    private Voter getVoterAnnotation(InvocationContext context) {
        Class<?> classType = context.getTarget().getClass();
        Method method = context.getMethod();
        return getVoterAnnotation(classType, method);
    }

    private Voter getVoterAnnotation(Class<?> someClassType, Method someMethod) {

        Voter result = someMethod.getAnnotation(Voter.class);

        if (result == null) {
            result = getAnnotation(someClassType, Voter.class);
        }
        return result;
    }

    private static <A extends Annotation> A getAnnotation(Class<?> someClass, Class<A> someAnnotation) {
        A result = null;
        if (someClass.isAnnotationPresent(someAnnotation)) {
            result = someClass.getAnnotation(someAnnotation);
        } else {
            if (someClass != Object.class) {
                result = getAnnotation(someClass.getSuperclass(), someAnnotation);
            }
        }
        return result;
    }

    public static <T> T getContextualReference(Class<T> type) {
        BeanManager beanManager = CDI.current().getBeanManager();

        Set<Bean<T>> beans = getBeanDefinitions(type, beanManager);

        Bean<T> bean = beans.iterator().next();
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        T result = (T) beanManager.getReference(bean, type, creationalContext);

        return result;
    }

    private static <T> Set<Bean<T>> getBeanDefinitions(Class<T> type,
                                                       BeanManager beanManager) {
        Set<Bean<?>> beans = beanManager.getBeans(type);

        if (beans == null || beans.isEmpty()) {
            throw new IllegalStateException("Could not find beans for Type=" + type);
        }

        Set<Bean<T>> result = new HashSet<>();

        for (Bean<?> bean : beans) {
            //noinspection unchecked
            @SuppressWarnings("unchecked")
            Bean<T> beanT = (Bean<T>) bean;
            result.add(beanT);
        }

        return result;
    }

}