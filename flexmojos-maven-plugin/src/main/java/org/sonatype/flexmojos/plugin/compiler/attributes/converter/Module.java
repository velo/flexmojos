package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

public class Module
{

    private String destinationPath;

    private String finalName;

    private Boolean optimize ;

    private String sourceFile;

    public String getDestinationPath()
    {
        return destinationPath;
    }

    public String getFinalName()
    {
        return finalName;
    }

    public String getSourceFile()
    {
        return sourceFile;
    }

    public Boolean isOptimize()
    {
        return optimize;
    }

    public void setDestinationPath( String destinationPath )
    {
        this.destinationPath = destinationPath;
    }

    public void setFinalName( String finalName )
    {
        this.finalName = finalName;
    }

    public void setOptimize( Boolean optimize )
    {
        this.optimize = optimize;
    }

    public void setSourceFile( String sourceFile )
    {
        this.sourceFile = sourceFile;
    }

}
