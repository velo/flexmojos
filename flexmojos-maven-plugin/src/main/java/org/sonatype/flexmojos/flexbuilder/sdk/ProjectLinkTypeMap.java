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
