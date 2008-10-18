package info.flexmojos.compatibilitykit.test;

import static info.flexmojos.compatibilitykit.VersionUtils.isMaxVersionOK;
import static info.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static info.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

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
