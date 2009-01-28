/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.sandbox.bundlepublisher.util.Xpp3Util;

public class Organization
{

    private Xpp3Dom dom;

    Organization( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getName()
    {
        return Xpp3Util.getValue( dom, "name" );
    }

    public String getLicense()
    {
        return Xpp3Util.getValue( dom, "license" );
    }

    public String getUrl()
    {
        return Xpp3Util.getValue( dom, "url" );
    }

}
