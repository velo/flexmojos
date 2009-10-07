package org.sonatype.flexmojos.generator.iface;

import java.util.Arrays;

import junit.framework.TestCase;

public class StringUtilTest
    extends TestCase
{

    public void testPrefixRemoval()
    {
        String a = "abcCba";
        assertEquals( "Cba", StringUtil.removePrefix( a ) );
        a = "abc3Cba";
        assertEquals( "Cba", StringUtil.removePrefix( a ) );
    }

    public void testSplit()
    {
        String a = "abcCba";
        assertEquals( Arrays.toString( new String[] { "abc", "cba" } ),
                      Arrays.toString( StringUtil.splitCamelCase( a ) ) );
    }

}
