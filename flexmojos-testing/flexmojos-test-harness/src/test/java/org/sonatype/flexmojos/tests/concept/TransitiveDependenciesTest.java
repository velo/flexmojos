package org.sonatype.flexmojos.tests.concept;

import org.testng.annotations.Test;

public class TransitiveDependenciesTest
    extends AbstractConceptTest
{
    @Test
    public void testTransitiveDependency()
        throws Exception
    {
        standardConceptTester( "transitive-dependencies" );
    }
}
