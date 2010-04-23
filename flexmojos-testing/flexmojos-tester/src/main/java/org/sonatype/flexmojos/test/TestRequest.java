/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
