package org.sonatype.flexmojos.generator.iface;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

public class StringUtilTest
{

    @Test
    public void testPrefixRemoval()
    {
        String a = "abcCba";
        assertEquals( "Cba", StringUtil.removePrefix( a ) );
        a = "abc3Cba";
        assertEquals( "Cba", StringUtil.removePrefix( a ) );
    }

    @Test
    public void testSplit()
    {
        String a = "abcCba";
        assertEquals( Arrays.toString( new String[] { "abc", "cba" } ),
                      Arrays.toString( StringUtil.splitCamelCase( a ) ) );
    }

}
