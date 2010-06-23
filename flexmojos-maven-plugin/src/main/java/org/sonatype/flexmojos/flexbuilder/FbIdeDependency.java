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
package org.sonatype.flexmojos.flexbuilder;

import org.apache.maven.plugin.ide.IdeDependency;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.flexbuilder.sdk.LinkType;
import org.sonatype.flexmojos.flexbuilder.sdk.LocalSdkEntry;

/**
 * Extends IdeDependency to add the scope value. Additionally
 * this extension handles resolving the path to the dependency
 * to fix a problem that was encountered with 64bit Windows.
 * 
 * @author Lance Linder llinder@gmail.com
 *
 */
public class FbIdeDependency extends IdeDependency
{
	private String path = null;
	private String sourcePath = null;
	private LocalSdkEntry localSdkEntry = null;
	
	/**
	 * Constructor that copies values from an existing IdeDependency
	 * instance and addes the scope value
	 * @param dep
	 * @param scope
	 */
	public FbIdeDependency(IdeDependency dep, String scope)
	{
		this( dep, scope, null );
	}
	
	public FbIdeDependency(IdeDependency dep, String scope, LocalSdkEntry entry)
	{
		this.setArtifactId( dep.getArtifactId() );
		this.setClassifier( dep.getClassifier() );
		this.setFile( dep.getFile() );
		this.setGroupId( dep.getGroupId() );
		this.setScope( scope );
		this.setType( dep.getType() );
		this.setVersion( dep.getVersion() );
		this.setEclipseProjectName( dep.getEclipseProjectName() );
		this.setAddedToClasspath( dep.isAddedToClasspath() );
    	if( dep.getSourceAttachment() != null )
    		this.setSourceAttachment( dep.getSourceAttachment().getAbsoluteFile() );
    	this.setReferencedProject( dep.isReferencedProject() );
    	this.localSdkEntry = entry;
	}
	
	/**
     * dependency scope
     */
	private String scope;
	
	/**
     * Getter for <code>scope</code>.
     * @return Returns the scope.
     */
    public String getScope()
    {
        return this.scope;
    }

    /**
     * Setter for <code>scope</code>.
     * @param scope The scope to set.
     */
    public void setScope( String scope )
    {
        this.scope = scope;
    }
    
    public LinkType getLinkType()
    {
    	LinkType type = LinkType.MERGE;
    	
    	if( scope.equals( FlexScopes.EXTERNAL ) || scope.equals( "runtime" ) ) {
    		type = LinkType.EXTERNAL;
    	} else if ( scope.equals( FlexScopes.RSL  ) ) {
    		type = LinkType.RSL;
    	} else if ( scope.equals( FlexScopes.CACHING ) ) {
    		type = LinkType.RSL_DIGEST;
    	} else {
    		type = LinkType.MERGE; // MERGED is 1. MERGED is default.
    	}
    	
    	return type;
    }
    
    public void setPath( String path )
    {
    	this.path = path;
    }
    public String getPath()
    {
    	if( localSdkEntry != null )
    	{
    		return localSdkEntry.getPath();
    	}
    	else if( path == null )
    	{
    		path = this.getFile().getAbsolutePath();
    	}
    	return path;
    }
    
    public void setSourcePath(String path)
    {
    	this.sourcePath = path;
    }
    
    public String getSourcePath()
    {   
    	if( localSdkEntry != null )
    	{
    		return localSdkEntry.getSourcePath();
    	}
    	    	
    	return sourcePath;
    }
    
    public boolean isFlexSdkDependency()
    {
    	return ( localSdkEntry != null || 
    			( "rb.swc".equals( getType() ) && "com.adobe.flex.framework".equals( getGroupId() ) ) );
    }
    
    public boolean isModifiedFlexSdkDependency()
    {
    	boolean modified = false;
    	
    	if( isFlexSdkDependency() )
    	{
    		// TODO resolve if this dependency is different from the base.
    	}
    	
    	return modified;
    }
    
    
}
