package org.sonatype.flexmojos.compiler.test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public interface MockitoConstraints
{
    Answer<Object> RETURNS_NULL = new Answer<Object>()
    {
        public Object answer( InvocationOnMock invocation )
            throws Throwable
        {
            return null;
        }
    };

}
