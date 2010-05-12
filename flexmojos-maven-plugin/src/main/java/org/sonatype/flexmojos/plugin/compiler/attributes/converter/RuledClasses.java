package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import org.apache.maven.model.PatternSet;

public class RuledClasses
{

    private String[] classes;

    private PatternSet[] classSets;

    public PatternSet[] getClassSets()
    {
        return classSets;
    }

    public void setClassSets( PatternSet[] classSets )
    {
        this.classSets = classSets;
    }

    public String[] getClasses()
    {
        return classes;
    }

    public void setClasses( String[] classes )
    {
        this.classes = classes;
    }

}
