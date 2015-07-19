package net.flexmojos.oss.plugin.air.packager;

import org.codehaus.plexus.component.annotations.Component;

import java.io.File;

/**
 * Created by christoferdutz on 18.07.15.
 */
@Component( role = Packager.class, hint = "linux-rpm" )
public class LinuxRpmPackager extends DesktopPackager {

    @Override
    protected File getOutputFile() {
        return new File(request.getBuildDir(), request.getFinalName() +
                ((request.getClassifier() != null) ? "-" + request.getClassifier() : "")  +".rpm");
    }

}
