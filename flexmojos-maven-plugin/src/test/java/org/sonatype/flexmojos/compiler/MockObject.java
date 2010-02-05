package org.sonatype.flexmojos.compiler;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;

public class MockObject
{

    @Test
    public void mockObject()
    {
        List<String> a = new ArrayList<String>( Arrays.asList( "a" ) );
        List<String> a1 = Mockito.spy( a );
        when( a1.toString() ).thenReturn( "33" );

        System.out.println( a );
        System.out.println( a1 );
    }

}
