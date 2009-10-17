package org.sonatype.flexmojos.htmlwrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Implements a java.util.Map facade on top of a Plexus Xpp3Dom object. Change requested to the facade's interface (for
 * example: put, remove) WILL affect the underlying Xpp3Dom object.
 * 
 * @author David Rom (david.s.rom@gmail.com)
 */
public class Xpp3DomMap
    implements Map<String, String>
{

    private Xpp3Dom source;

    public Xpp3DomMap( Xpp3Dom source )
    {
        this.source = source;
    }

    /**
     * Not supported
     */
    public void clear()
    {
    }

    public boolean containsKey( Object key )
    {
        return source.getChild( (String) key ) != null;
    }

    public boolean containsValue( Object value )
    {
        return values().contains( value );
    }

    public Set<Entry<String, String>> entrySet()
    {
        Xpp3Dom[] children = source.getChildren();
        HashSet<Entry<String, String>> retVal = new HashSet<Entry<String, String>>( children.length );

        for ( int i = 0; i < children.length; i++ )
        {
            Xpp3Dom child = children[i];
            retVal.add( new XppEntry( child ) );
        }

        return retVal;
    }

    public String get( Object key )
    {
        Xpp3Dom child = source.getChild( (String) key );
        if ( child != null )
        {
            return child.getValue();
        }

        return null;
    }

    public boolean isEmpty()
    {
        return source.getChildCount() == 0;
    }

    public Set<String> keySet()
    {
        Xpp3Dom[] children = source.getChildren();
        HashSet<String> retVal = new HashSet<String>( children.length );

        for ( int i = 0; i < children.length; i++ )
        {
            Xpp3Dom child = children[i];
            retVal.add( child.getName() );
        }

        return retVal;
    }

    public String put( String key, String value )
    {
        if ( key == null )
            return null;

        Xpp3Dom child = source.getChild( key );
        if ( child == null )
        {
            child = new Xpp3Dom( key );
            source.addChild( child );
        }

        String lastValue = child.getValue();
        child.setValue( value );

        return lastValue;
    }

    public void putAll( Map<? extends String, ? extends String> m )
    {
        for ( Map.Entry<? extends String, ? extends String> e : m.entrySet() )
            put( e.getKey(), e.getValue() );
    }

    public String remove( Object key )
    {
        for ( int i = 0; i < source.getChildCount(); ++i )
        {
            Xpp3Dom child = source.getChild( i );
            if ( child.getName() != null && child.getName().equals( key ) )
            {
                source.removeChild( i );
                return child.getValue();
            }
        }

        return null;
    }

    public int size()
    {
        return source.getChildCount();
    }

    public Collection<String> values()
    {
        Xpp3Dom[] children = source.getChildren();
        ArrayList<String> retVal = new ArrayList<String>( children.length );

        for ( int i = 0; i < children.length; i++ )
        {
            Xpp3Dom child = children[i];
            retVal.set( i, child.getValue() );
        }

        return retVal;
    }

    class XppEntry
        implements Entry<String, String>
    {

        Xpp3Dom entry;

        XppEntry( Xpp3Dom entry )
        {
            this.entry = entry;
        }

        public String getKey()
        {
            return entry.getName();
        }

        public String getValue()
        {
            return entry.getValue();
        }

        public String setValue( String value )
        {
            String lastValue = entry.getValue();
            entry.setValue( value );
            return lastValue;
        }
    }
}
