package org.sonatype.flexmojos.compiler.lazyload;

import java.util.Map;

public interface Cacheable
{

    public Map<String, Object> getCache();

}
