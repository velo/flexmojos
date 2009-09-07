package org.sonatype.flexmojos.tests.concept;

import org.apache.maven.it.Verifier;
import org.testng.annotations.Test;

public class TransitiveDependenciesTest
    extends AbstractConceptTest
{
    @Test
    public void testTransitiveDependency()
        throws Exception
    {
        Verifier v = standardConceptTester( "transitive-dependencies" );
    }
}
