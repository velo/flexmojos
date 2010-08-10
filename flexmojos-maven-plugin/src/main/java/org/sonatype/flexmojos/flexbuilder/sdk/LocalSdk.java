package org.sonatype.flexmojos.flexbuilder.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sonatype.flexmojos.flexbuilder.FbIdeDependency;
import org.sonatype.flexmojos.flexbuilder.ProjectType;


public class LocalSdk
{
	public static final String SDK_GROUP_ID = "com.adobe.flex.framework";
	
	public enum Version
	{
		FLEX3_0_0("3.0.0"),
		FLEX3_1_0("3.1.0"),
		FLEX3_2_0("3.2.0"),
		FLEX3_3_0("3.3.0"),
		FLEX3_4_0("3.4.0"),
		FLEX3_5_0("3.5.0"),
		FLEX4_0_0("4.0.0");
		
		public static Version valueFrom( String version )
		{
			int l = Version.values().length;
			for( int i=0; i<l; i++ )
			{
				Version v = Version.values()[i];
				if( v.getVersion().equals( version ) )
					return v;
			}
			
			return null;
		}
		
		private final String version;
		private Version( String version )
		{
			this.version = version;
		}
		public String getVersion()
		{
			return this.version;
		}
	}

	private HashMap<String, LocalSdkEntry> entries;
	private List<LocalSdkEntry> entryList;
	private String version;
	private ProjectType projectType;
	private List<LocalSdkEntry> includedEntries;
	private List<LocalSdkEntry> excludedEntries;
	private List<LocalSdkEntry> modifiedEntries;
	
	public LocalSdk( String version, ProjectType projectType )
	{
		this.projectType = projectType;
		this.version = version;
		entries = getEntries( version );
		entryList = new ArrayList<LocalSdkEntry>( entries.values() );
	}
	
	/**
	 * Returns a list of local SDK entries that are part of the Maven dependency collection.
	 * 
	 * @param dependencies
	 * @return
	 */
	public List<LocalSdkEntry> getIncludes( Collection<FbIdeDependency> dependencies )
	{
		if( includedEntries == null )
			getExcludes( dependencies );
		
		return includedEntries;
	}
	
	/**
	 * Returns a list of local SDK entries that are not part of the Maven dependency collection.
	 * 
	 * @param dependencies
	 * @return
	 */
	public List<LocalSdkEntry> getExcludes( Collection<FbIdeDependency> dependencies )
	{
		if( excludedEntries == null )
		{
			includedEntries = new ArrayList<LocalSdkEntry>();
			modifiedEntries = new ArrayList<LocalSdkEntry>();
		
			// Loop through and collect all local SDK entries that are in the Maven dependency collection
			Iterator<FbIdeDependency> iter = dependencies.iterator();
			while( iter.hasNext() )
			{
				FbIdeDependency dep = iter.next();
				if( "com.adobe.flex.framework".equals( dep.getGroupId() ) && "swc".equals( dep.getType() ) )
				{
					LocalSdkEntry localEntry = entries.get( dep.getArtifactId() );
					dep.setLocalSdkEntry( localEntry );
					if( localEntry != null && localEntry.isProjectType() )
					{
						includedEntries.add( localEntry );
						
						// Add to modified entries if local SDK values differ from the Maven dependency values
						if( localEntry.isModified( dep ) )
						{
							modifiedEntries.add( localEntry );
						}
					}
				}
			}
			Collections.sort( includedEntries );
			
			// Loop through and collect all local SDK entries that are not in the Maven dependency collection
			excludedEntries = new ArrayList<LocalSdkEntry>( entryList );
			Iterator<LocalSdkEntry> localIter = excludedEntries.iterator();
			while( localIter.hasNext() )
			{
				LocalSdkEntry localEntry = localIter.next();
				if( localEntry == null || includedEntries.contains( localEntry ) || !localEntry.isProjectType() )
					localIter.remove();
			}
		}
		
		return excludedEntries;
	}
	
	public List<LocalSdkEntry> getModified( Collection<FbIdeDependency> dependencies )
	{
		if( modifiedEntries == null )
			getExcludes( dependencies );
		
		return modifiedEntries;
	}
	
	public LocalSdkEntry getEntry( String artifactId )
	{
		return entries.get( artifactId );
	}
	
	public LinkType getDefaultLinkType()
	{
		Version v = getBestVersion( version );
		
		LinkType type = LinkType.MERGE;
		
		switch( v )
		{
		case FLEX4_0_0:
			type = LinkType.RSL; // Yes it is RSL and NOT RSL_DIGEST.
			break;
		case FLEX3_5_0:
		case FLEX3_4_0:
		case FLEX3_3_0:
		case FLEX3_2_0:
		case FLEX3_1_0:
		case FLEX3_0_0:
			type = LinkType.MERGE;
			break;
		default:
			type = LinkType.MERGE;
			break;
		}
		
		return type;
	}
	
	private Version getBestVersion( String version )
	{
		// Find closest matching configuration.
		Version v = Version.valueFrom( version );
		while( v == null )
		{
			version = version.substring( 0, version.lastIndexOf(".") );
			v = Version.valueFrom( version );
		}
		return v;
	}
	
	private HashMap<String, LocalSdkEntry> getEntries( String version )
	{
		HashMap<String, LocalSdkEntry>map = new HashMap<String, LocalSdkEntry>();
		
		Version v = getBestVersion( version );
		
		switch( v )
		{
		case FLEX4_0_0:
			// No longer part of standard dependencies. Path changed.
			if( !map.containsKey( "automation") )
				map.put( "automation",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation/src",
						projectType,
						null
						) );
			// No longer part of standard dependencies. Path changed.
			if( !map.containsKey( "automation_agent") )
				map.put( "automation_agent",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_agent",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation_agent.swc",
						null,
						projectType,
						null
						) );
			// New
			if( !map.containsKey( "automation_air") )
				map.put( "automation_air",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_air",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation_air.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation_air/src",
						projectType,
						null
						) );
			// New
			if( !map.containsKey( "automation_airspark") )
				map.put( "automation_airspark",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_airspark",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation_airspark.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation_airspark/src",
						projectType,
						null
						) );
			// No longer part of standard dependencies. Path changed.
			if( !map.containsKey( "automation_dmv") )
				map.put( "automation_dmv",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_dmv",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation_dmv.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation_dmv/src",
						projectType,
						null
						) );
			// No longer part of standard dependencies. Path changed.
			if( !map.containsKey( "automation_flashflexkit") )
				map.put( "automation_flashflexkit",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_flashflexkit",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation_flashflexkit.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation_flashflexkit/src",
						projectType,
						null
						) );
			// New
			if( !map.containsKey( "automation_spark") )
				map.put( "automation_spark",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_spark",
						"${PROJECT_FRAMEWORKS}/libs/automation/automation_spark.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation_spark/src",
						projectType,
						null
						) );
			// New
			if( !map.containsKey( "qtp_air") )
				map.put( "qtp_air",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"qtp_air",
						"${PROJECT_FRAMEWORKS}/libs/automation/qtp_air.swc",
						null,
						projectType,
						null
						) );
		    // No longer part of standard dependencies. Path changed.
			if( !map.containsKey( "qtp") )
				map.put( "qtp",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"qtp",
						"${PROJECT_FRAMEWORKS}/libs/automation/qtp.swc",
						null,
						projectType,
						null
						) );
			// Source path changed
			if( !map.containsKey( "datavisualization") )
				map.put( "datavisualization",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"datavisualization",
						"${PROJECT_FRAMEWORKS}/libs/datavisualization.swc",
						"${PROJECT_FRAMEWORKS}/projects/datavisualization/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE, 7 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE, 7 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			// New
			if( !map.containsKey( "flash-integration") )
				map.put( "flash-integration",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"flash-integration",
						"${PROJECT_FRAMEWORKS}/libs/flash-integration.swc",
						"${PROJECT_FRAMEWORKS}/projects/flash-integration/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE, 8 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE, 8 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.MERGE ) )
						) );
			// New
			if( !map.containsKey( "framework") )
				map.put( "framework",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"framework",
						"${PROJECT_FRAMEWORKS}/libs/framework.swc",
						"${PROJECT_FRAMEWORKS}/projects/framework/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.RSL_DIGEST, 3 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 3 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			// No longer part of the SDK
			if( !map.containsKey( "haloclassic") )
				map.put( "haloclassic", null );
			// New
			if( !map.containsKey( "osmf") )
				map.put( "osmf",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"osmf",
						"${PROJECT_FRAMEWORKS}/libs/osmf.swc",
						"${PROJECT_FRAMEWORKS}/projects/osmf/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.RSL_DIGEST, 2 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 2 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ),
								new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.MERGE ) )
						) );
			// Changed link type
			if( !map.containsKey( "rpc") )
				map.put( "rpc",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"rpc",
						"${PROJECT_FRAMEWORKS}/libs/rpc.swc",
						"${PROJECT_FRAMEWORKS}/projects/rpc/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.RSL_DIGEST, 6 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 6 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			// New
			if( !map.containsKey( "spark") )
				map.put( "spark",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"spark",
						"${PROJECT_FRAMEWORKS}/libs/spark.swc",
						"${PROJECT_FRAMEWORKS}/projects/spark/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.RSL_DIGEST, 4 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 4 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			// New
			if( !map.containsKey( "sparkskins") )
				map.put( "sparkskins",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"sparkskins",
						"${PROJECT_FRAMEWORKS}/libs/sparkskins.swc",
						"${PROJECT_FRAMEWORKS}/projects/sparkskins/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.RSL_DIGEST, 5 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 5 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			// New
			if( !map.containsKey( "textLayout") )
				map.put( "textLayout",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"textLayout",
						"${PROJECT_FRAMEWORKS}/libs/textLayout.swc",
						"${PROJECT_FRAMEWORKS}/projects/textLayout/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.RSL_DIGEST, 1 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 1 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			// Index changed.
			if( !map.containsKey( "utilities") )
				map.put( "utilities",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"utilities",
						"${PROJECT_FRAMEWORKS}/libs/utilities.swc",
						"${PROJECT_FRAMEWORKS}/projects/utilities/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE, 9 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE, 9 ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
		case FLEX3_5_0:
		case FLEX3_4_0:
			// Datavisulization source now included.
			if( !map.containsKey( "datavisualization") )
				map.put( "datavisualization",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"datavisualization",
						"${PROJECT_FRAMEWORKS}/libs/datavisualization.swc",
						"${PROJECT_FRAMEWORKS}/projects/datavisualisation/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
		case FLEX3_3_0:
		case FLEX3_2_0:
		case FLEX3_1_0:
		case FLEX3_0_0:
			if( !map.containsKey( "airframework") )
				map.put( "airframework",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"airframework",
						"${PROJECT_FRAMEWORKS}/libs/air/airframework.swc",
						"${PROJECT_FRAMEWORKS}/projects/airframework/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "airglobal") )
				map.put( "airglobal",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"airglobal",
						"${PROJECT_FRAMEWORKS}/libs/air/airglobal.swc",
						null,
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.EXTERNAL ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			if( !map.containsKey( "applicationupdater") )
				map.put( "applicationupdater",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"applicationupdater",
						"${PROJECT_FRAMEWORKS}/libs/air/applicationupdater.swc",
						"${PROJECT_FRAMEWORKS}/projects/air/ApplicationUpdater/src/ApplicationUpdater",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "applicationupdater_ui") )
				map.put( "applicationupdater_ui",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"applicationupdater_ui",
						"${PROJECT_FRAMEWORKS}/libs/air/applicationupdater_ui.swc",
						"${PROJECT_FRAMEWORKS}/projects/air/ApplicationUpdater/src/ApplicationUpdaterDialogs",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "automation") )
				map.put( "automation",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation",
						"${PROJECT_FRAMEWORKS}/libs/automation.swc",
						"${PROJECT_FRAMEWORKS}/projects/automation/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "automation_agent") )
				map.put( "automation_agent",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_agent",
						"${PROJECT_FRAMEWORKS}/libs/automation_agent.swc",
						null,
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "automation_dmv") )
				map.put( "automation_dmv",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_dmv",
						"${PROJECT_FRAMEWORKS}/libs/automation_dmv.swc",
						null,
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "automation_flashflexkit") )
				map.put( "automation_flashflexkit",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"automation_flashflexkit",
						"${PROJECT_FRAMEWORKS}/libs/automation_flashflexkit.swc",
						null,
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "flex") )
				map.put( "flex",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"flex",
						"${PROJECT_FRAMEWORKS}/libs/flex.swc",
						"${PROJECT_FRAMEWORKS}/projects/flex/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "framework") )
				map.put( "framework",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"framework",
						"${PROJECT_FRAMEWORKS}/libs/framework.swc",
						"${PROJECT_FRAMEWORKS}/projects/framework/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			if( !map.containsKey( "haloclassic") )
				map.put( "haloclassic",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"haloclassic",
						"${PROJECT_FRAMEWORKS}/themes/HaloClassic/haloclassic.swc",
						"${PROJECT_FRAMEWORKS}/projects/haloclassic/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			if( !map.containsKey( "playerglobal") )
				map.put( "playerglobal",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"playerglobal",
						null,
						null,
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.EXTERNAL, 0 ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL, 0 ),
								new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.EXTERNAL, 0 ) )
						) );
			if( !map.containsKey( "rpc") )
				map.put( "rpc",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"rpc",
						"${PROJECT_FRAMEWORKS}/libs/rpc.swc",
						"${PROJECT_FRAMEWORKS}/projects/rpc/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.EXTERNAL ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.EXTERNAL ) )
						) );
			if( !map.containsKey( "servicemonitor") )
				map.put( "servicemonitor",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"servicemonitor",
						"${PROJECT_FRAMEWORKS}/libs/air/servicemonitor.swc",
						"${PROJECT_FRAMEWORKS}/projects/air/ServiceMonitor/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );
			if( !map.containsKey( "utilities") )
				map.put( "utilities",
						new LocalSdkEntry(
						SDK_GROUP_ID,
						"utilities",
						"${PROJECT_FRAMEWORKS}/libs/utilities.swc",
						"${PROJECT_FRAMEWORKS}/projects/utilities/src",
						projectType,
						new ProjectLinkTypeMap(
								new ProjectLinkTypeEntry( ProjectType.ACTIONSCRIPT, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.FLEX_LIBRARY, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR, LinkType.MERGE ),
								new ProjectLinkTypeEntry( ProjectType.AIR_LIBRARY, LinkType.MERGE ) )
						) );

		}
	
		return map;
	}

}
