package net.flexmojos.oss.plugin.air.packager;

import org.codehaus.plexus.component.annotations.Component;

import java.io.File;

/**
 * Created by christoferdutz on 18.07.15.
 */
@Component( role = Packager.class, hint = "windows" )
public class WindowsPackager extends DesktopPackager {

    @Override
    protected File getOutputFile() {
        return new File(request.getBuildDir(), request.getFinalName() +
                ((request.getClassifier() != null) ? "-" + request.getClassifier() : "")  +".exe");
    }

    @Override
    public File execute() throws PackagingException {
        if (!System.getProperty("os.name").startsWith("Win")) {
            throw new PackagingException("Can't create Windows packages on a non windows machine.");
        }
        return super.execute();
    }

}
