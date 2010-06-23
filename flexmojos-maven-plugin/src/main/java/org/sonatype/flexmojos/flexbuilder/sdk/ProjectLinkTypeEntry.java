package org.sonatype.flexmojos.flexbuilder.sdk;

import org.sonatype.flexmojos.flexbuilder.ProjectType;

public class ProjectLinkTypeEntry
{
	private ProjectType projectType;
	private LinkType linkType;
	private Integer index;
	
	public ProjectLinkTypeEntry( ProjectType projectType, LinkType linkType )
	{
		this(projectType, linkType, null);
	}
	public ProjectLinkTypeEntry( ProjectType projectType, LinkType linkType, Integer index )
	{
		this.projectType = projectType;
		this.linkType = linkType;
		this.index = index;
	}
	
	public ProjectType getProjectType()
	{
		return projectType;
	}
	
	public LinkType getLinkType()
	{
		return linkType;
	}
	
	public Integer getIndex()
	{
		return index;
	}
}
