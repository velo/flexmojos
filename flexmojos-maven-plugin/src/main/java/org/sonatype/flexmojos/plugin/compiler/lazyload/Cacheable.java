package org.sonatype.flexmojos.plugin.compiler.lazyload;

import java.util.Map;

public interface Cacheable
{

    @NotCacheable
    public Map<String, Object> getCache();

}
