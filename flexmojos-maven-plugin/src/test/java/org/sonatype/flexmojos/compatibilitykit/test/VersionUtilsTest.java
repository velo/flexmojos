/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compatibilitykit.test;

import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMaxVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VersionUtilsTest
{

    @Test
    public void splitVersions()
    {
        Assert.assertTrue( Arrays.equals( new int[] { 3, 0, 0, 477 }, splitVersion( "3.0.0.477" ) ) );
        Assert.assertTrue( Arrays.equals( new int[] { 2, 0, 1 }, splitVersion( "2.0.1" ) ) );
        Assert.assertTrue( Arrays.equals( new int[] { 3, 1 }, splitVersion( "3.1" ) ) );
        Assert.assertTrue( Arrays.equals( new int[] { 4 }, splitVersion( "4" ) ) );
        Assert.assertTrue( Arrays.equals( new int[] { 3, 0, 1, 1092 }, splitVersion( "3.0.1.1092-flexcover" ) ) );
    }

    @Test
    public void testMinVersion()
    {
        Assert.assertTrue( isMinVersionOK( splitVersion( "2.0.1" ), splitVersion( "2.0.0" ) ) );
        Assert.assertTrue( isMinVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "2" ) ) );
        Assert.assertTrue( isMinVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "3" ) ) );

        Assert.assertFalse( isMinVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "3.1" ) ) );
        Assert.assertFalse( isMinVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "4" ) ) );
        Assert.assertTrue( isMinVersionOK( splitVersion( "3.1.0" ), splitVersion( "3.0.0" ) ) );
    }

    @Test
    public void testMaxVersion()
    {
        Assert.assertFalse( isMaxVersionOK( splitVersion( "2.0.1" ), splitVersion( "2.0.0" ) ) );
        Assert.assertFalse( isMaxVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "2" ) ) );
        Assert.assertTrue( isMaxVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "3" ) ) );

        Assert.assertTrue( isMaxVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "3.1" ) ) );
        Assert.assertTrue( isMaxVersionOK( splitVersion( "3.0.0.477" ), splitVersion( "4" ) ) );
        Assert.assertFalse( isMaxVersionOK( splitVersion( "3.1.0" ), splitVersion( "3.0.0" ) ) );
    }
}
