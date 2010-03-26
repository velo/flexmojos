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

    private int firstConnectionTimeout;

    private File swf;

    private int testControlPort;

    private int testPort;

    private int testTimeout;

    private String flashplayerCommand;

    private boolean allowHeadlessMode = true;

    /**
     * @return the firstConnectionTimeout
     */
    public int getFirstConnectionTimeout()
    {
        return firstConnectionTimeout;
    }

    public File getSwf()
    {
        return swf;
    }

    /**
     * @return the testControlPort
     */
    public int getTestControlPort()
    {
        return testControlPort;
    }

    /**
     * @return the testPort
     */
    public int getTestPort()
    {
        return testPort;
    }

    /**
     * @return the testTimeout
     */
    public int getTestTimeout()
    {
        return testTimeout;
    }

    /**
     * @param firstConnectionTimeout the firstConnectionTimeout to set
     */
    public void setFirstConnectionTimeout( int firstConnectionTimeout )
    {
        this.firstConnectionTimeout = firstConnectionTimeout;
    }

    public void setSwf( File swf )
    {
        this.swf = swf;
    }

    /**
     * @param testControlPort the testControlPort to set
     */
    public void setTestControlPort( int testControlPort )
    {
        this.testControlPort = testControlPort;
    }

    /**
     * @param testPort the testPort to set
     */
    public void setTestPort( int testPort )
    {
        this.testPort = testPort;
    }

    /**
     * @param testTimeout the testTimeout to set
     */
    public void setTestTimeout( int testTimeout )
    {
        this.testTimeout = testTimeout;
    }

    public String getFlashplayerCommand()
    {
        return this.flashplayerCommand;
    }

    public void setFlashplayerCommand( String flashplayerCommand )
    {
        this.flashplayerCommand = flashplayerCommand;
    }

    public boolean getAllowHeadlessMode()
    {
        return this.allowHeadlessMode;
    }

    public void setAllowHeadlessMode( boolean allowHeadlessMode )
    {
        this.allowHeadlessMode = allowHeadlessMode;
    }
}
