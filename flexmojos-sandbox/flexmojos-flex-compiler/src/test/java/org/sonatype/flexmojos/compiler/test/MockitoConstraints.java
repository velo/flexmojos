package org.sonatype.flexmojos.compiler.test;

import org.mockito.ReturnValues;
import org.mockito.invocation.InvocationOnMock;

public interface MockitoConstraints
{
    ReturnValues RETURNS_NULL = new ReturnValues()
    {
        public Object valueFor( InvocationOnMock invocation )
        {
            return null;
        }
    };
}
