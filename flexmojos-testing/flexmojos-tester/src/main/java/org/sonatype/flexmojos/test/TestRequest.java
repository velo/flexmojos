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
package org.sonatype.flexmojos.test;

import java.io.File;

public class TestRequest
{

    private String adlCommand;

    private boolean allowHeadlessMode = true;

    private int firstConnectionTimeout;

    private String flashplayerCommand;

    private File swf;

    private File swfDescriptor;

    private int testControlPort;

    private int testPort;

    private int testTimeout;

    private boolean useAirDebugLauncher;

    public String getAdlCommand()
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

    public String getFlashplayerCommand()
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

    public void setAdlCommand( String adlCommand )
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

    public void setFlashplayerCommand( String flashplayerCommand )
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
