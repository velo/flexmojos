package org.sonatype.flexmojos.compiler.util;

import java.util.List;

public interface FlexCompilerArgumentParser
{

    public abstract <E> String[] parseArguments( E cfg, Class<? extends E> configClass );

    public abstract <E> String[] parseArguments( E cfg, Class<? extends E> configClass, ClassLoader classLoader );

    public abstract <E> List<String> getArgumentsList( E cfg, Class<? extends E> configClass );

    public abstract <E> List<String> getArgumentsList( E cfg, Class<? extends E> configClass, ClassLoader classLoader );

}