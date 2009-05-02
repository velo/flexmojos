/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import flex2.tools.oem.Builder;
import flex2.tools.oem.Report;

public class FlexConfigBuilder
{
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

    public void parse( String config )
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

        Element element = new Element( "output", namespace );
        element.addContent( output.getAbsolutePath() );
        rootElement.addContent( element );
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
            Element child = new Element( childName, namespace );
            child.addContent( value );
            element.addContent( child );
        }
        rootElement.addContent( element );
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

    public void addIncludeFiles( String[] names, String[] paths )
    {
        for ( int i = 0; i < names.length; i++ )
        {
            Element element = new Element( "include-file", namespace );

            Element name = new Element( "name", namespace );
            name.addContent( names[i] );
            element.addContent( name );

            Element path = new Element( "path", namespace );
            path.addContent( paths[i] );
            element.addContent( path );

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

        Element pathElement = new Element( "path-element", namespace );
        pathElement.addContent( path );

        sourceElement.addContent( pathElement );
    }

    public void addSourcePath( File file )
    {
        addSourcePath( file.getAbsolutePath() );
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
        return rootElement.getChild( name, namespace ) != null;
    }

    private boolean exists( String name, Element parent )
    {
        return parent.getChild( name, namespace ) != null;
    }
}
