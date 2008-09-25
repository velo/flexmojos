package info.flexmojos.compatibilitykit;

import org.apache.maven.plugin.Mojo;

public interface FlexMojo extends Mojo
{

    /**
     * @return Flex SDK version on x.x.x format
     */
    public String getFDKVersion();

}
