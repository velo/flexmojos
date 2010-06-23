/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.flexbuilder;

import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;





/**
 * Extends the standard Flex Builder configuration.
 * 
 * @author Lance Linder (llinder@gmail.com)
 * @since 3.5
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal flashbuilder
 * @requiresDependencyResolution
 */
public class FlashbuilderMojo
    extends FlexbuilderMojo
{
	/**
	 * IDE Verion. First release of Flashbuilder is version 6.
	 * 
	 * @parameter default-value=6
	 */
	protected Integer ideVersion;
	
	@Override
	protected String getAsPropertiesTemplate()
	{
		return "/templates/flashbuilder/actionScriptProperties.vm";
	}
	
	@Override
	protected VelocityContext getAsPropertiesContext(ProjectType type, Collection<FbIdeDependency> dependencies)
		throws MojoExecutionException
	{
		VelocityContext context = super.getAsPropertiesContext( type, dependencies );
		
		context.put( "ideVersion", ideVersion );
		
		return context;
	}
	
	@Override
	protected String getFlexLibPropertiesTemplate()
	{
		return "/templates/flashbuilder/flexLibProperties.vm";
	}
	
	@Override
	protected String getFlexPropertiesTemplate()
	{
		return "/templates/flashbuilder/flexProperties.vm";
	}
	
	/**
     * Utility function to sense flex builder SDK value from framework dependencies.
     * 
     * For example:
     * 4.0.0 will import into Flexbuilder as "Flex 4.0"
     * 3.5.0 will import as Flex "3.5".
     * 
     * This override differs from the base function in that versions such
     * as 4.0.0 will return 4.0 instead of 4 as in the base implementation.
     * 
     * @return
     */
	@Override
    protected String getFlexSdkVersion()
    {
    	String value = super.getFlexSdkVersion();
    	
    	if( !value.equals( "default" ) && !value.contains(".") )
    		value += ".0";
    	
    	return value;
    }

}
