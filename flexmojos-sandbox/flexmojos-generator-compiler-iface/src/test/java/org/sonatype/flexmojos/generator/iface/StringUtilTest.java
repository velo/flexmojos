package org.sonatype.flexmojos.generator.iface;

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

}
