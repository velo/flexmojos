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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos334Test
    extends AbstractIssueTest
{

    @Test
    public void linkReportScanner()
        throws Exception
    {
        FMVerifier v =
            testIssue( "flexmojos-334", "-Dflex.coverageStrategy=link-report", "-Dflex.coverageOverwriteSourceRoots=src/main/flex" );
        List<String> classes = getCoveredClasses( v );
        Assert.assertEquals( classes.size(), 3, classes.toString() );
        // bug on flexmojos/cobertura support, it does think the files are .java
        MatcherAssert.assertThat( classes, IsCollectionContaining.hasItems( "FlexMaven/UntestedClass.java",
                                                                            "FlexMaven/sampleInclude.java",
                                                                            "FlexMaven/App.java" ) );
        MatcherAssert.assertThat( classes,
                                  CoreMatchers.not( IsCollectionContaining.hasItems( "FlexMaven/unusedInclude.java" ) ) );
    }

    @Test
    public void disabledScanner()
        throws Exception
    {
        FMVerifier v =
            testIssue( "flexmojos-334", "-Dflex.coverageStrategy=disabled", "-Dflex.coverageOverwriteSourceRoots=src/main/flex" );
        List<String> classes = getCoveredClasses( v );
        Assert.assertEquals( classes.size(), 2, classes.toString() );
        // bug on flexmojos/cobertura support, it does think the files are .java
        MatcherAssert.assertThat( classes,
                                  IsCollectionContaining.hasItems( "FlexMaven/sampleInclude.java", "FlexMaven/App.java" ) );
        MatcherAssert.assertThat( classes,
                                  CoreMatchers.not( IsCollectionContaining.hasItems( "FlexMaven/UntestedClass.java",
                                                                                     "FlexMaven/unusedInclude.java" ) ) );
    }

    @Test(enabled=false)
    //Flex doesn't the inclusions from sourcePath, it must be at same folder as the main.mxml...
    public void as3contentScanner()
        throws Exception
    {
        FMVerifier v =
            testIssue( "flexmojos-334", "-Dflex.coverageStrategy=as3Content", "-Dflex.coverageOverwriteSourceRoots=src/main/flex" );
        List<String> classes = getCoveredClasses( v );
        Assert.assertEquals( classes.size(), 4, classes.toString() );
        // bug on flexmojos/cobertura support, it does think the files are .java
        MatcherAssert.assertThat( classes, IsCollectionContaining.hasItems( "FlexMaven/UntestedClass.java",
                                                                            "FlexMaven/sampleInclude.java",
                                                                            "FlexMaven/unusedInclude.java",
                                                                            "FlexMaven/App.java" ) );
    }

    @SuppressWarnings( "unchecked" )
    private List<String> getCoveredClasses( FMVerifier v )
        throws DocumentException
    {
        File coverageXml = new File( v.getBasedir(), "target/coverage/coverage.xml" );

        MatcherAssert.assertThat( coverageXml, FileMatcher.exists() );

        List<String> linkedFiles = new ArrayList<String>();
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read( coverageXml );

        List<Attribute> list = document.selectNodes( "//class/@filename" );
        for ( Attribute attribute : list )
        {
            linkedFiles.add( attribute.getValue() );
        }
        return linkedFiles;
    }
}
