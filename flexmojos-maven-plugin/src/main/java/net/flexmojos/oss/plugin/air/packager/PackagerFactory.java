package net.flexmojos.oss.plugin.air.packager;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Map;

/**
 * Created by christoferdutz on 16.07.15.
 */
@Component( role = PackagerFactory.class )
public class PackagerFactory {

    @Requirement(role = Packager.class)
    private Map<String, Packager> packagers;

    public Packager getPackager(PackagingRequest packagingRequest) {
        //return new MacPackager();
        return packagers.get(packagingRequest.getTargetPlatform().toLowerCase());
    }

}
