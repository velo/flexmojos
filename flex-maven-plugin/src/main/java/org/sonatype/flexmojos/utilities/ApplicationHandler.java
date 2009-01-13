/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.utilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ApplicationHandler
    extends DefaultHandler
{
    /**
     * The MXML XML namespace
     */
    private static final String ADOBE_MXML_APPLICATION_ELEMENT = "Application";

    /**
     * The MXML application XML element
     */
    private static final String ADOBE_MXML_NAMESPACE = "http://www.adobe.com/2006/mxml";

    private boolean rc = false;

    private boolean first = true;

    /**
     * Internal constructor
     */
    public ApplicationHandler()
    {
        super();
    }

    /**
     * Start element handler. Will analyze the first time it is called if the element tag is 'Application' to detect
     * MXML files representing application files.
     * 
     * @param uri the XML namespace of the element
     * @param localName the local name of the element
     * @param name the name of the element
     * @param attributes the XML attributes of the element
     */
    @Override
    public void startElement( String uri, String localName, String name, Attributes attributes )
        throws SAXException
    {
        if ( first )
        {
            first = false;
            if ( ADOBE_MXML_NAMESPACE.equals( uri ) && ADOBE_MXML_APPLICATION_ELEMENT.equals( localName ) )
            {
                rc = true;
            }

        }
    }

    /**
     * Flag to tag the MXML file as an application one
     * 
     * @return the application flag value
     */
    public boolean isApplicationFile()
    {
        return rc;
    }

}
