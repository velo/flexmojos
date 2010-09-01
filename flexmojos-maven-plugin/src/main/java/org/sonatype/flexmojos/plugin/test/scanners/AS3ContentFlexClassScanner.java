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
package org.sonatype.flexmojos.plugin.test.scanners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = FlexClassScanner.class, hint = "as3Content" )
public class AS3ContentFlexClassScanner
    extends AbstractFlexClassScanner
{
    private static final String COMMENTS_REGEX = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";

    private ArrayList<String> sniplets;

    public void scan( File[] directories, String[] exclusions, Map<String, Object> context )
    {
        classes = new ArrayList<String>();
        sniplets = new ArrayList<String>();

        for ( File dir : directories )
        {
            List<String> found = scan( dir, exclusions, context );
            destinateAs3Files( dir, found );
        }
    }

    protected void destinateAs3Files( File basedir, List<String> found )
    {
        for ( String includedFile : found )
        {
            try
            {
                if ( isClassFile( includedFile, basedir ) )
                {
                    classes.add( includedFile );
                }
                else
                {
                    sniplets.add( includedFile );
                }
            }
            catch ( IOException e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().error( "Error reading class content " + includedFile, e );
                }
                else
                {
                    getLogger().error( "Error reading class content " + includedFile );
                }
            }
        }
    }

    private boolean isClassFile( String pathname, File basedir )
        throws IOException
    {
        if ( pathname.endsWith( ".mxml" ) )
            return true;

        File file = new File( basedir, pathname );
        String contents = FileUtils.readFileToString( file );
        contents = contents.replaceAll( COMMENTS_REGEX, "$1 " );

        String className = FilenameUtils.getBaseName( file.getName() );
        String matchRegex = "[\\s]*(class|interface)[\\s]+" + className + "[\\s]*";

        Pattern pattern = Pattern.compile( matchRegex );
        Matcher matcher = pattern.matcher( contents );

        return matcher.find();
    }

    public List<String> getAs3Snippets()
    {
        return sniplets;
    }
}
