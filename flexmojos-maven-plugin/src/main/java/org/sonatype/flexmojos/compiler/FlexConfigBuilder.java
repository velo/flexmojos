/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sonatype.flexmojos.test.util.PathUtil;

import flex2.compiler.config.ConfigurationBuffer;
import flex2.tools.oem.Application;
import flex2.tools.oem.Builder;
import flex2.tools.oem.Configuration;
import flex2.tools.oem.Library;
import flex2.tools.oem.Logger;
import flex2.tools.oem.Report;
import flex2.tools.oem.internal.OEMConfiguration;
import flex2.tools.oem.internal.OEMUtil;

public class FlexConfigBuilder
{
    private static final String LINK_REPORT = "link-report";

    private static final String RESOURCE_BUNDLE_REPORT = "resource-bundle-list";

    private Namespace namespace;

    private Document document;

    private Element rootElement;

    public FlexConfigBuilder( String config )
        throws IOException, JDOMException
    {
        parse( config );
    }

    public FlexConfigBuilder( Builder builder )
        throws IOException, JDOMException
    {
        Report report = builder.getReport();
        StringWriter stringWriter = new StringWriter();
        report.writeConfigurationReport( stringWriter );
        parse( stringWriter.toString() );
    }

    public FlexConfigBuilder( Application builder )
        throws IOException, JDOMException
    {
        parse( builder.getConfiguration(), builder.getLogger(), true );
    }

    public FlexConfigBuilder( Library builder )
        throws IOException, JDOMException
    {
        parse( builder.getConfiguration(), builder.getLogger(), false );
    }

    public FlexConfigBuilder( Configuration configuration, Logger logger, Boolean isApplication )
        throws IOException, JDOMException
    {
        parse( configuration, logger, isApplication );
    }

    private void parse( Configuration configuration, Logger logger, Boolean isApplication )
        throws IOException, JDOMException
    {
        // we need open issue "feature request", but currently we use workaround
        OEMConfiguration oemConfiguration = (OEMConfiguration) configuration;

        OEMConfiguration tempOEMConfiguration;
        if ( isApplication )
        {
            String[] options = oemConfiguration.getCompilerOptions();
            String[] tempOptions = new String[options.length + 2];
            System.arraycopy( options, 0, tempOptions, 0, options.length );
            tempOptions[tempOptions.length - 2] = "--file-specs";
            tempOptions[tempOptions.length - 1] = "fake.as";

            tempOEMConfiguration = OEMUtil.getApplicationConfiguration( tempOptions, true, logger, null, null );
        }
        else
        {
            tempOEMConfiguration =
                OEMUtil.getLibraryConfiguration( oemConfiguration.getCompilerOptions(), true, logger, null, null );
        }
        parse( tempOEMConfiguration.cfgbuf );
    }

    private void parse( ConfigurationBuffer configurationBuffer )
        throws IOException, JDOMException
    {
        parse( OEMUtil.formatConfigurationBuffer( configurationBuffer ) );
    }

    private void parse( String config )
        throws IOException, JDOMException
    {
        SAXBuilder parser = new SAXBuilder();
        document = parser.build( new StringReader( config ) );

        // https://bugs.adobe.com/jira/browse/SDK-19122
        rootElement = document.getRootElement();
        if ( rootElement.getNamespace() == Namespace.NO_NAMESPACE )
        {
            namespace = Namespace.getNamespace( "http://www.adobe.com/2006/flex-config" );
            rootElement.setNamespace( namespace );
        }

        removeCommentsAndSetNamespace( document.getContent() );

        if ( namespace == null )
        {
            namespace = rootElement.getNamespace();
        }
    }

    public void write( Writer writer )
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setIndent( "\t" );
        outputter.setFormat( format );

        outputter.output( document, writer );
        writer.flush();
    }

    public void write( File file )
        throws IOException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter( file );
            write( writer );
            writer.close();
        }
        finally
        {
            IOUtils.closeQuietly( writer );
        }
    }

    public void addOutput( File output )
    {
        if ( exists( "output" ) )
        {
            return;
        }

        addText( "output", output.getAbsolutePath() );
    }

    /**
     * https://bugs.adobe.com/jira/browse/FCM-15
     * 
     * @param output output file
     * @param home base path, should be a directory, not a file, or it doesn't make sense
     */
    public void addOutput( File output, File home )
    {
        if ( exists( "output" ) )
        {
            return;
        }

        addText( "output", PathUtil.getRelativePath( home, output ) );
    }

    public void addList( String[] list, String rootName, String childName )
    {
        if ( exists( rootName ) )
        {
            return;
        }

        Element element = new Element( rootName, namespace );
        for ( String value : list )
        {
            addText( element, childName, value );
        }
        rootElement.addContent( element );
    }

    public void addList( List<String> list, String rootName, String childName )
    {
        addList( list.toArray( new String[list.size()] ), rootName, childName );
    }

    public void append( String value, String rootName, String childName )
    {
        Element parent = rootElement.getChild( rootName, namespace );
        if ( parent == null )
        {
            parent = new Element( rootName, namespace );
            rootElement.addContent( parent );
        }

        addText( parent, childName, value );
    }

    public void addList( File[] list, String rootName, String childName )
    {
        String[] paths = new String[list.length];
        for ( int i = 0; i < list.length; i++ )
        {
            paths[i] = list[i].getAbsolutePath();
        }
        addList( paths, rootName, childName );
    }

    public void addIncludeFiles( List<String> names, List<String> paths )
    {
        for ( int i = 0, namesSize = names.size(); i < namesSize; i++ )
        {
            Element element = new Element( "include-file", namespace );
            addText( element, "name", names.get( i ) );
            addText( element, "path", paths.get( i ) );
            rootElement.addContent( element );
        }
    }

    public void addEmptyLocale()
    {
        Element compilerElement = rootElement.getChild( "compiler", namespace );
        if ( exists( "locale", compilerElement ) )
        {
            return;
        }
        compilerElement.addContent( new Element( "locale", namespace ) );
    }

    public void addSourcePath( String path )
    {
        Element sourceElement = rootElement.getChild( "compiler", namespace ).getChild( "source-path", namespace );
        addText( sourceElement, "path-element", path );
    }

    public void addSourcePath( File file )
    {
        addSourcePath( file.getAbsolutePath() );
    }

    public void addLinkReport( String pathname )
    {
        addText( LINK_REPORT, pathname );
    }

    public void addResourceBundleReport( String pathname )
    {
        addText( RESOURCE_BUNDLE_REPORT, pathname );
    }

    private void addText( String name, String value )
    {
        addText( rootElement, name, value );
    }

    private void addText( Element parent, String name, String value )
    {
        Element element = new Element( name, namespace );
        element.addContent( value );
        parent.addContent( element );
    }

    @SuppressWarnings( "unchecked" )
    private void removeCommentsAndSetNamespace( List l )
    {
        for ( Iterator i = l.iterator(); i.hasNext(); )
        {
            Object node = i.next();
            if ( node instanceof Comment )
            {
                i.remove();
            }
            else if ( node instanceof Element )
            {
                Element element = (Element) node;
                if ( namespace != null )
                {
                    element.setNamespace( namespace );
                }
                removeCommentsAndSetNamespace( element.getContent() );
            }
        }
    }

    private boolean exists( String name )
    {
        return exists( name, rootElement );
    }

    private boolean exists( String name, Element parent )
    {
        return parent.getChild( name, namespace ) != null;
    }
}
