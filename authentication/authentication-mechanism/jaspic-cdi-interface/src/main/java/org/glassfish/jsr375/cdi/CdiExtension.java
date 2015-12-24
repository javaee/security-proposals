package org.glassfish.jsr375.cdi;

import static org.glassfish.jsr375.cdi.CdiUtils.getAnnotation;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.annotation.BasicAuthenticationMechanismDefinition;

import org.glassfish.jsr375.mechanisms.BasicAuthenticationMechanism;

public class CdiExtension implements Extension {

    // Note: for now use the highlander rule: "there can be only one" for
    // both identity stores and (http) authentication mechanisms.
    // This could be extended later to support multiple
    private Bean<HttpAuthenticationMechanism> authenticationMechanismBean;
    private boolean httpAuthenticationMechanismFound;

    public <T> void processBean(@Observes ProcessBean<T> eventIn, BeanManager beanManager) {

        ProcessBean<T> event = eventIn; // JDK8 u60 workaround

        // TODO: 
        // * What if multiple definitions present?
        // *   -> Make created Bean<T>s alternatives
        // *   -> Throw exception?
        
        
        Optional<BasicAuthenticationMechanismDefinition> optionalBasicMechanism = getAnnotation(beanManager, event.getAnnotated(), BasicAuthenticationMechanismDefinition.class);
        if (optionalBasicMechanism.isPresent()) {
            authenticationMechanismBean = new CdiProducer<HttpAuthenticationMechanism>()
                .scope(ApplicationScoped.class)
                .types(HttpAuthenticationMechanism.class)
                .addToId(BasicAuthenticationMechanismDefinition.class)
                .create(e -> new BasicAuthenticationMechanism(optionalBasicMechanism.get().realmName()));
        }
        
        if (event.getBean().getTypes().contains(HttpAuthenticationMechanism.class)) {
            // enabled bean implementing the HttpAuthenticationMechanism found
            httpAuthenticationMechanismFound = true;
        }
        
    }

    public void afterBean(final @Observes AfterBeanDiscovery afterBeanDiscovery) {
        if (authenticationMechanismBean != null) {
            afterBeanDiscovery.addBean(authenticationMechanismBean);
        }
    }
    
    public boolean isHttpAuthenticationMechanismFound() {
        return httpAuthenticationMechanismFound;
    }

}
