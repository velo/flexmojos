package org.sonatype.flexmojos.compiler;

import flex2.compiler.Logger;
import flex2.compiler.common.SinglePathResolver;

public class CompilerThreadLocal
{
    // I HATE THIS PATTERN
    public static final ThreadLocal<Logger> logger = new ThreadLocal<Logger>();

    public static final ThreadLocal<SinglePathResolver> pathResolver = new ThreadLocal<SinglePathResolver>();

}
