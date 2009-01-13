/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
