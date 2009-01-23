/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.generator;

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
