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
package org.sonatype.flexmojos.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.sonatype.flexmojos.common.FlexDependencySorter;

import eu.cedarsoft.utils.ZipExtractor;

public class FDKConfigResolver
{
    private File configDirectory;

    private Document config;

    private boolean isAIR;

    private String[] fontManagers;

    private List<Namespace> namespaces;

    public FDKConfigResolver( FlexDependencySorter dependencySorter, Build build )
        throws MojoExecutionException
    {
        this.isAIR = dependencySorter.isAIR();

        configDirectory = new File( build.getOutputDirectory(), "config-" + dependencySorter.getFDKVersion() );
        if ( !configDirectory.exists() )
        {
            resolveConfigDirectory( dependencySorter.getFDKConfigFile() );
        }
    }

    private void resolveConfigDirectory( File configFile )
        throws MojoExecutionException
    {
        if ( configFile != null )
        {
            ZipExtractor zipExtractor;
            // noinspection ResultOfMethodCallIgnored
            configDirectory.mkdirs();
            try
            {
                zipExtractor = new ZipExtractor( configFile );
                zipExtractor.extract( configDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to extract SDK configuration", e );
            }
        }
    }

    private Document getConfig()
        throws MojoExecutionException
    {
        if ( config == null )
        {
            File configFile = new File( configDirectory, ( isAIR ? "air" : "flex" ) + "-config.xml" );
            if ( !configFile.isFile() )
            {
                throw new MojoExecutionException( "Unable to find SDK config file " + configFile );
            }

            try
            {
                SAXBuilder parser = new SAXBuilder();
                config = parser.build( configFile );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Error parsing " + configFile, e );
            }
        }

        return config;
    }

    @SuppressWarnings( "unchecked" )
    public String[] getFontManagers()
        throws MojoExecutionException
    {
        if ( fontManagers == null )
        {
            if ( !configDirectory.exists() )
            {
                return null;
            }

            Element node = getConfig().getRootElement().getChild( "compiler" );
            if ( node == null )
            {
                return null;
            }
            node = node.getChild( "fonts" );
            if ( node == null )
            {
                return null;
            }
            node = node.getChild( "managers" );
            if ( node == null )
            {
                return null;
            }

            List<String> managers = new ArrayList<String>();
            for ( Element element : (Iterable<Element>) node.getChildren() )
            {
                managers.add( element.getValue() );
            }

            fontManagers = managers.toArray( new String[managers.size()] );
        }
        return fontManagers;
    }

    @SuppressWarnings( "unchecked" )
    public List<Namespace> getNamespaces()
        throws MojoExecutionException
    {
        if ( namespaces == null )
        {
            if ( !configDirectory.exists() )
            {
                return null;
            }

            List<Namespace> namespaces = new ArrayList<Namespace>();

            Element node = getConfig().getRootElement().getChild( "compiler" );
            if ( node == null )
            {
                return null;
            }
            node = node.getChild( "namespaces" );
            if ( node == null )
            {
                return null;
            }

            for ( Element element : (Iterable<Element>) node.getChildren() )
            {
                Element uriNode = element.getChild( "uri" );
                Element manifestNode = element.getChild( "manifest" );
                File manifest = new File( configDirectory, manifestNode.getValue() );
                namespaces.add( new Namespace( uriNode.getValue(), manifest ) );
            }

            this.namespaces = namespaces;
        }
        return namespaces;

    }
}
