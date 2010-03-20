/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compiler.attributes;

public class MavenArtifact
{

    private String artifactId;

    private String classifier;

    private String groupId;

    private String type;

    private String version;

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getType()
    {
        return type;
    }

    public String getVersion()
    {
        return version;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

}
