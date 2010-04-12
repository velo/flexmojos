package org.sonatype.flexmojos;

import org.granite.messaging.service.annotations.RemoteDestination;

@RemoteDestination
public class SimpleRemoteDestination {
    
    public String sayHello(String name) {
        return "Hello " + name;
    }

}
