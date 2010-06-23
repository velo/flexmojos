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
package org.sonatype.flexmojos.flexbuilder.sdk;

import java.util.HashMap;

import org.sonatype.flexmojos.flexbuilder.ProjectType;

public class ProjectLinkTypeMap extends HashMap<ProjectType, ProjectLinkTypeEntry>
{

	private static final long serialVersionUID = -8120824098108250551L;
	
	public ProjectLinkTypeMap( ProjectLinkTypeEntry ... entries )
	{
		for( int i=0; i<entries.length; i++ )
		{
			ProjectLinkTypeEntry entry = entries[i];
			
			this.put( entry.getProjectType(), entry );
		}
		
	}
	
	public boolean contains( ProjectType projectType )
	{
		return this.containsKey( projectType );
	}
}
