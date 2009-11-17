/**
 * 
 */
package org.sonatype.flexmojos.compiler.command;

import java.util.List;

import flex2.tools.Compiler;

public class MxmlcCommand
    implements Command
{

    private String[] args;

    public MxmlcCommand( List<String> args )
    {
        this.args = args.toArray( new String[args.size()] );
    }

    public void command()
    {
        Compiler.mxmlc( args );
    }
}