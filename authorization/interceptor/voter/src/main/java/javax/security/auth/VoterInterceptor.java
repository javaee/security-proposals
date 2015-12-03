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