package org.sonatype.flexmojos.test;

import java.io.File;

public class TestRequest
{

    private String[] adlCommand;

    private boolean allowHeadlessMode = true;

    private int firstConnectionTimeout;

    private String[] flashplayerCommand;

    private File swf;

    private File swfDescriptor;

    private int testControlPort;

    private int testPort;

    private int testTimeout;

    private boolean useAirDebugLauncher;

    public String[] getAdlCommand()
    {
        return adlCommand;
    }

    public boolean getAllowHeadlessMode()
    {
        return this.allowHeadlessMode;
    }

    public int getFirstConnectionTimeout()
    {
        return firstConnectionTimeout;
    }

    public String[] getFlashplayerCommand()
    {
        return this.flashplayerCommand;
    }

    public File getSwf()
    {
        return swf;
    }

    public File getSwfDescriptor()
    {
        return swfDescriptor;
    }

    public int getTestControlPort()
    {
        return testControlPort;
    }

    public int getTestPort()
    {
        return testPort;
    }

    public int getTestTimeout()
    {
        return testTimeout;
    }

    public boolean getUseAirDebugLauncher()
    {
        return useAirDebugLauncher;
    }

    public void setAdlCommand( String[] adlCommand )
    {
        this.adlCommand = adlCommand;
    }

    public void setAllowHeadlessMode( boolean allowHeadlessMode )
    {
        this.allowHeadlessMode = allowHeadlessMode;
    }

    public void setFirstConnectionTimeout( int firstConnectionTimeout )
    {
        this.firstConnectionTimeout = firstConnectionTimeout;
    }

    public void setFlashplayerCommand( String[] flashplayerCommand )
    {
        this.flashplayerCommand = flashplayerCommand;
    }

    public void setSwf( File swf )
    {
        this.swf = swf;
    }

    public void setSwfDescriptor( File swfDescriptor )
    {
        this.swfDescriptor = swfDescriptor;
    }

    public void setTestControlPort( int testControlPort )
    {
        this.testControlPort = testControlPort;
    }

    public void setTestPort( int testPort )
    {
        this.testPort = testPort;
    }

    public void setTestTimeout( int testTimeout )
    {
        this.testTimeout = testTimeout;
    }

    public void setUseAirDebugLauncher( boolean useAirDebugLauncher )
    {
        this.useAirDebugLauncher = useAirDebugLauncher;
    }
}
