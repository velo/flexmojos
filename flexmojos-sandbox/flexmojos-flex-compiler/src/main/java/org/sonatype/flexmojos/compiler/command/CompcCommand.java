/**
 * 
 */
package org.sonatype.flexmojos.compiler.command;

import java.util.List;

import flex2.tools.Compc;

public class CompcCommand
    implements Command
{

    private String[] args;

    public CompcCommand( List<String> args )
    {
        this.args = args.toArray( new String[args.size()] );
    }

    public void command()
    {
        Compc.compc( args );
    }
}