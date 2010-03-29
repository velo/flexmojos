package flex2.tools.oem.internal;

import flex2.tools.oem.Message;

class GenericMessage
    implements Message
{
    private String level;

    private String path;

    private String message;

    private int line;

    private int col;

    GenericMessage( Message message )
    {
        this( message.getLevel(), message.getPath(), message.getLine(), message.getColumn(), message.toString() );
    }

    GenericMessage( String level, String path, int line, int col, String message )
    {
        this.level = level;
        this.path = path;
        this.line = line;
        this.col = col;
        this.message = message;
    }

    public int getColumn()
    {
        return this.col;
    }

    public String getLevel()
    {
        return this.level;
    }

    public int getLine()
    {
        return this.line;
    }

    public String getPath()
    {
        return this.path;
    }

    public String toString()
    {
        return this.message;
    }
}