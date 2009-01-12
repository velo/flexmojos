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
package info.flexmojos.generator;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.granite.generator.Listener;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.exception.TemplateUriException;

public class Gas3GroovyTransformer
    extends JavaAs3GroovyTransformer
{
    private String[] outputClasses;

    public Gas3GroovyTransformer( JavaAs3GroovyConfiguration config, Listener listener, String[] outputClasses )
    {
        super( config, listener );
        this.outputClasses = outputClasses;
    }

    @Override
    protected JavaAs3Output[] getOutputs( JavaAs3Input input )
        throws IOException, TemplateUriException
    {
        if ( matchWildCard( input.getType().getName(), outputClasses ) )
        {
            return super.getOutputs( input );
        }

        return new JavaAs3Output[0];
    }

    private boolean matchWildCard( String className, String[] wildCards )
    {
        if ( wildCards == null )
        {
            return true;
        }

        for ( String wildCard : wildCards )
        {
            if ( FilenameUtils.wildcardMatch( className, wildCard ) )
                return true;
        }

        return false;
    }
}
