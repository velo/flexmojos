package org.sonatype.flexmojos.bcel;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class ByteClassLoader extends ClassLoader
{

    public ByteClassLoader( String name, byte[] bytecode, ClassRealm realm )
    {
        super(realm);
        
        defineClass( name, bytecode, 0, bytecode.length );
    }

}
