package org.sonatype.flexmojos.configurator;

public class ConfiguratorException
    extends Exception
{

    private static final long serialVersionUID = 2029345547281621644L;

    public ConfiguratorException()
    {
        super();
    }

    public ConfiguratorException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ConfiguratorException( String message )
    {
        super( message );
    }

}
