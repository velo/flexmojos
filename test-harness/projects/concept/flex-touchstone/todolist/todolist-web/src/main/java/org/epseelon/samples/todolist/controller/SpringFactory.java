package org.epseelon.samples.todolist.controller;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.ServiceException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringFactory implements FlexFactory {
    private static final String SOURCE = "source";

    public void initialize(String id, ConfigMap configMap) {
    }

    public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
        SpringFactoryInstance instance = new SpringFactoryInstance(this, id, properties);
        instance.setSource(properties.getPropertyAsString(SOURCE, instance.getId()));
        return instance;
    } // end method createFactoryInstance()

    public Object lookup(FactoryInstance inst) {
        SpringFactoryInstance factoryInstance = (SpringFactoryInstance) inst;
        return factoryInstance.lookup();
    }

    static class SpringFactoryInstance extends FactoryInstance {
        SpringFactoryInstance(SpringFactory factory, String id, ConfigMap properties) {
            super(factory, id, properties);
        }

        public String toString() {
            return "SpringFactory instance for id=" + getId() + " source=" + getSource() + " scope=" +
                    getScope();
        }

        public Object lookup() {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(flex.messaging.FlexContext.getServletConfig().getServletContext());

            String beanName = getSource();
            try {
                return appContext.getBean(beanName);
            } catch (NoSuchBeanDefinitionException nexc) {
                ServiceException e = new ServiceException();
                String msg = "Spring service named '" + beanName
                        + "' does not exist.";
                e.setMessage(msg);
                e.setRootCause(nexc);
                e.setDetails(msg);
                e.setCode("Server.Processing");

                throw e;
            } catch (BeansException bexc) {
                ServiceException e = new ServiceException();
                String msg = "Unable to create Spring service named '"
                        + beanName + "' ";
                e.setMessage(msg);
                e.setRootCause(bexc);
                e.setDetails(msg);
                e.setCode("Server.Processing");
                throw e;
            }
        }
    }
}
