package org.sonatype.flexmojos.common.converter;

import org.apache.maven.model.FileSet;

public class RuledClasses
{

    private String[] classes;

    private FileSet[] classSets;

    public FileSet[] getClassSets()
    {
        return classSets;
    }

    public void setClassSets( FileSet[] classSets )
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
