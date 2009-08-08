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

        Assert.assertTrue( isMinVersionOK( splitVersion( "4.0.0-SNAPSHOT" ), splitVersion( "3.1" ) ) );
        Assert.assertTrue( isMinVersionOK( splitVersion( "4.0.0-SNAPSHOT" ), splitVersion( "4.0.0-SNAPSHOT" ) ) );
        Assert.assertTrue( isMinVersionOK( splitVersion( "4.0.0" ), splitVersion( "4.0.0" ) ) );
        Assert.assertTrue( isMinVersionOK( splitVersion( "4.0.0-SNAPSHOT" ), splitVersion( "3.0.0-SNAPSHOT" ) ) );

        Assert.assertFalse( isMinVersionOK( splitVersion( "3.0.0" ), splitVersion( "3.1.0" ) ) );
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

        Assert.assertFalse( isMaxVersionOK( splitVersion( "4.0.0-SNAPSHOT" ), splitVersion( "3.1" ) ) );
        Assert.assertTrue( isMaxVersionOK( splitVersion( "3.1" ), splitVersion( "4.0.0-SNAPSHOT" ) ) );
    }
}
