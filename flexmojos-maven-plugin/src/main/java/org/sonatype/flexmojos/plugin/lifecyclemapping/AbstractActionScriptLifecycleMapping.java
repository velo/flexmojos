package org.sonatype.flexmojos.plugin.lifecyclemapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.lifecycle.mapping.Lifecycle;

public abstract class AbstractActionScriptLifecycleMapping
{

    private Map<String, Lifecycle> lifecycleMap;

    public AbstractActionScriptLifecycleMapping()
    {
        super();
    }

    public Map<String, Lifecycle> getLifecycles()
    {
        if ( lifecycleMap != null )
        {
            return lifecycleMap;
        }

        lifecycleMap = new LinkedHashMap<String, Lifecycle>();
        Lifecycle lifecycle = new Lifecycle();

        lifecycle.setId( "default" );
        Map<String, String> phases = new LinkedHashMap<String, String>();
        phases.put( "process-resources", "org.apache.maven.plugins:maven-resources-plugin:resources" );
        phases.put( "compile", getCompiler() );
        phases.put( "process-test-resources", "org.apache.maven.plugins:maven-resources-plugin:testResources" );
        phases.put( "test-compile", "org.sonatype.flexmojos:flexmojos-maven-plugin:test-compile" );
        phases.put( "test", "org.sonatype.flexmojos:flexmojos-maven-plugin:test-run" );
        if ( getPackage() != null )
        {
            phases.put( "package", getPackage() );
        }
        phases.put( "install", "org.apache.maven.plugins:maven-install-plugin:install" );
        phases.put( "deploy", "org.apache.maven.plugins:maven-deploy-plugin:deploy" );
        phases.put( "verify", "org.sonatype.flexmojos:flexmojos-maven-plugin:unused-check" );
        lifecycle.setPhases( phases );

        lifecycleMap.put( "default", lifecycle );
        return lifecycleMap;
    }

    protected String getPackage()
    {
        return null;
    }

    public abstract String getCompiler();

    public List<String> getOptionalMojos( String lifecycle )
    {
        return null;
    }

    public Map<String, String> getPhases( String lifecycle )
    {
        Lifecycle lifecycleMapping = getLifecycles().get( lifecycle );

        if ( lifecycleMapping != null )
        {
            return lifecycleMapping.getPhases();
        }

        return null;
    }

}