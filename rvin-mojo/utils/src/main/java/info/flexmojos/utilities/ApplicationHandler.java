/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.utilities;

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
